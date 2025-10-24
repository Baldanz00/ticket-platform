package m4.gioia.dashboard_gestione_tickets.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;


import m4.gioia.dashboard_gestione_tickets.model.Ticket;
import m4.gioia.dashboard_gestione_tickets.model.User;
import m4.gioia.dashboard_gestione_tickets.repository.CategoryRepository;
import m4.gioia.dashboard_gestione_tickets.repository.TicketRepository;
import m4.gioia.dashboard_gestione_tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("")
public class RestApiController {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String getHome(Model model, Authentication authentication) {
        int totalTicketsCompleted;
        int totalTicketsInProgress;
        int totalTicketsToDo;
        int totalTickets;

        if (getAuthorityFromAuthentication(authentication, "ADMIN")) {
            totalTicketsCompleted = ticketRepository.findTicketByStatus(Ticket.Status.COMPLETATO).size();
            totalTicketsInProgress = ticketRepository.findTicketByStatus(Ticket.Status.IN_CORSO).size();
            totalTicketsToDo = ticketRepository.findTicketByStatus(Ticket.Status.DA_FARE).size();
        } else {
            totalTicketsCompleted = ticketRepository
                    .findTicketByStatusAndUser_Username(Ticket.Status.COMPLETATO, authentication.getName()).size();
            totalTicketsInProgress = ticketRepository
                    .findTicketByStatusAndUser_Username(Ticket.Status.IN_CORSO, authentication.getName())
                    .size();
            totalTicketsToDo = ticketRepository
                    .findTicketByStatusAndUser_Username(Ticket.Status.DA_FARE, authentication.getName()).size();
        }
        totalTickets = totalTicketsCompleted + totalTicketsInProgress + totalTicketsToDo;

        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("completedTickets", totalTicketsCompleted);
        model.addAttribute("inProgressTickets", totalTicketsInProgress);
        model.addAttribute("toDoTickets", totalTicketsToDo);
        return "index";
    }

    @GetMapping("/tickets")
    public String getTickets(Model model, @RequestParam(name = "keyword", required = false) String keyword,
            Authentication authentication) {
        Set<Ticket> ticketToShow = new HashSet<>();
        boolean hasAuthorityOperator = getAuthorityFromAuthentication(authentication, "OPERATOR");
        boolean hasAuthorityAdmin = getAuthorityFromAuthentication(authentication, "ADMIN");

        if (hasAuthorityOperator) {
            ticketToShow.addAll(ticketRepository.findByUser_Username(authentication.getName()));
        }

        if (hasAuthorityAdmin) {
            ticketToShow.addAll(ticketRepository.findAll());
        }

        if (keyword != null) {
            for (Ticket ticket : ticketToShow) {
                if (!ticket.getTitolo().contains(keyword)) {
                    ticketToShow.remove(ticket);
                }
            }
            model.addAttribute("ticketList", ticketToShow);
            return "tickets/displayTickets";
        }

        model.addAttribute("ticketList", ticketToShow);
        return "tickets/displayTickets";
    }

    @GetMapping("tickets/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        ticketRepository.deleteById(id);
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        Ticket ticket = optTicket.get();
        model.addAttribute("isAdmin", getAuthorityFromAuthentication(authentication, "ADMIN"));
        model.addAttribute("isOperator", getAuthorityFromAuthentication(authentication, "OPERATOR"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.Status.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));

        return "/tickets/editTicket";
    }

    @PostMapping("/tickets/edit/{id}")
    public String update(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult, Model model,
            Authentication authentication) {
        Ticket oldTicket = ticketRepository.findById(formTicket.getId()).get();
        Optional<User> userOpt = userRepository.findByUsername(formTicket.getUser().getUsername());

        formTicket.setCreationDate(oldTicket.getDataCreazione());
        System.out.println("user= " + formTicket.getUser());

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", getAuthorityFromAuthentication(authentication, "ADMIN"));
            model.addAttribute("isOperator", getAuthorityFromAuthentication(authentication, "OPERATOR"));
            model.addAttribute("ticket", oldTicket);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.Status.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "/tickets/editTicket";
        }

        ticketRepository.save(formTicket);

        User userFound;

        if (userOpt.isPresent()) {
            userFound = userOpt.get();
        } else {
            userFound = oldTicket.getUser();
        }

        if (formTicket.getStato().equals(Ticket.Status.DA_FARE)
                || formTicket.getStato().equals(Ticket.Status.IN_CORSO)) {
            userFound.setStatus(User.UserStatus.DISPONIBILE);
            userRepository.save(userFound);
        }

        return "redirect:/tickets";
    }

    @GetMapping("/tickets/create")
    public String getTicketsForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.Status.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
        model.addAttribute("ticket", new Ticket());
        return "tickets/ticketsForm";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model, Authentication authentication) {
        Optional<Ticket> optTicket = ticketRepository.findByTitle(formTicket.getTitolo());
        Optional<User> userOpt = userRepository.findByUsername(formTicket.getUser().getUsername());
        if (optTicket.isPresent()) {
            bindingResult.addError(new ObjectError("title", "There's already a ticket for this problem!"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.Status.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "tickets/ticketsForm";
        }

        formTicket.setCreationDate(LocalDate.now());
        ticketRepository.save(formTicket);
        if (userOpt.isPresent()) {
            User userFound = userOpt.get();
            if (formTicket.getStato().equals(Ticket.Status.DA_FARE)
                    || formTicket.getStato().equals(Ticket.Status.IN_CORSO)) {
                userFound.setStatus(User.UserStatus.DISPONIBILE);
                userRepository.save(userFound);
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Ticket created successfully");
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String showTicket(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        model.addAttribute("empty", ticketOpt.isEmpty());

        if (ticketOpt.isPresent()) {
            model.addAttribute("ticket", ticketOpt.get());
        }

        return "tickets/ticketDetail";

    }

    private boolean getAuthorityFromAuthentication(Authentication authentication, String authName) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(authName)) {
                return true;
            }
        }
        return false;
    }
}
