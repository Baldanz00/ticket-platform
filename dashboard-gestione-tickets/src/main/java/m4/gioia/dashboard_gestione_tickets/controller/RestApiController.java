package m4.gioia.dashboard_gestione_tickets.controller;

import java.time.Instant;
import java.util.*;

import com.esercizio.milestone.ticket_platform.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.esercizio.milestone.ticket_platform.model.Ticket;
import com.esercizio.milestone.ticket_platform.repository.CategoryRepository;
import com.esercizio.milestone.ticket_platform.repository.TicketRepository;
import com.esercizio.milestone.ticket_platform.repository.UserRepository;

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
            totalTicketsCompleted = ticketRepository.findTicketByStatus(Ticket.TicketStatus.COMPLETED).size();
            totalTicketsInProgress = ticketRepository.findTicketByStatus(Ticket.TicketStatus.IN_PROGRESS).size();
            totalTicketsToDo = ticketRepository.findTicketByStatus(Ticket.TicketStatus.TO_DO).size();
        } else {
            totalTicketsCompleted = ticketRepository
                    .findTicketByStatusAndUser_Username(Ticket.TicketStatus.COMPLETED, authentication.getName()).size();
            totalTicketsInProgress = ticketRepository
                    .findTicketByStatusAndUser_Username(Ticket.TicketStatus.IN_PROGRESS, authentication.getName())
                    .size();
            totalTicketsToDo = ticketRepository
                    .findTicketByStatusAndUser_Username(Ticket.TicketStatus.TO_DO, authentication.getName()).size();
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
                if (!ticket.getTitle().contains(keyword)) {
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
        model.addAttribute("statusNamesList", Ticket.TicketStatus.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));

        return "/tickets/editTicket";
    }

    @PostMapping("/tickets/edit/{id}")
    public String update(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult, Model model,
            Authentication authentication) {
        Ticket oldTicket = ticketRepository.findById(formTicket.getId()).get();
        Optional<User> userOpt = userRepository.findByUsername(formTicket.getUser().getUsername());

        formTicket.setCreationDate(oldTicket.getCreationDate());
        System.out.println("user= " + formTicket.getUser());

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", getAuthorityFromAuthentication(authentication, "ADMIN"));
            model.addAttribute("isOperator", getAuthorityFromAuthentication(authentication, "OPERATOR"));
            model.addAttribute("ticket", oldTicket);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.TicketStatus.values());
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

        if (formTicket.getStatus().equals(Ticket.TicketStatus.TO_DO)
                || formTicket.getStatus().equals(Ticket.TicketStatus.IN_PROGRESS)) {
            userFound.setStatus(User.UserStatus.AVAILABLE);
            userRepository.save(userFound);
        }

        return "redirect:/tickets";
    }

    @GetMapping("/tickets/create")
    public String getTicketsForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statusNamesList", Ticket.TicketStatus.values());
        model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
        model.addAttribute("ticket", new Ticket());
        return "tickets/ticketsForm";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model, Authentication authentication) {
        Optional<Ticket> optTicket = ticketRepository.findByTitle(formTicket.getTitle());
        Optional<User> userOpt = userRepository.findByUsername(formTicket.getUser().getUsername());
        if (optTicket.isPresent()) {
            bindingResult.addError(new ObjectError("title", "There's already a ticket for this problem!"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statusNamesList", Ticket.TicketStatus.values());
            model.addAttribute("operators", userRepository.findByRoles_Name("OPERATOR"));
            return "tickets/ticketsForm";
        }

        formTicket.setCreationDate(Instant.now());
        ticketRepository.save(formTicket);
        if (userOpt.isPresent()) {
            User userFound = userOpt.get();
            if (formTicket.getStatus().equals(Ticket.TicketStatus.TO_DO)
                    || formTicket.getStatus().equals(Ticket.TicketStatus.IN_PROGRESS)) {
                userFound.setStatus(User.UserStatus.AVAILABLE);
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
