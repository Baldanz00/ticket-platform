package m4.gioia.dashboard_gestione_tickets.controller;

import m4.gioia.dashboard_gestione_tickets.model.Ticket;
import m4.gioia.dashboard_gestione_tickets.model.User;
import m4.gioia.dashboard_gestione_tickets.repository.TicketRepository;
import m4.gioia.dashboard_gestione_tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserApiController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping("/profile")
    public String getProfilePage(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        boolean canUpdateStatus = ticketRepository.findTicketByStatusAndUser_Username(Ticket.Status.DA_FARE, username).size()
                                + ticketRepository.findTicketByStatusAndUser_Username(Ticket.Status.IN_CORSO, username).size() == 0;

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            model.addAttribute("userName", user.getName());
            model.addAttribute("userSurname", user.getSurname());
            model.addAttribute("userStatus", user.getStatus());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("canUpdateStatus", canUpdateStatus);
            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getName().toString()) // o r.getName().toString() a seconda della tua entità Role
                    .collect(Collectors.toList());
            model.addAttribute("userRoles", roles);
        }
        return "/users/profile";
    }

    @PostMapping("/profile/update-status")
    public String updateStatus(@RequestParam("status") String status,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Controlla se l'utente vuole diventare NON DISPONIBILE / INACTIVE
            if (status.equalsIgnoreCase("INACTIVE")) {

                // Recupera ticket attivi
                List<Ticket> activeTickets = ticketRepository.findByUserIdAndStatusIn(
                        user.getId(),
                        Arrays.asList(Ticket.Status.DA_FARE, Ticket.Status.IN_CORSO)
                );

                if (!activeTickets.isEmpty()) {
                    // Non permettere l'aggiornamento
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Cannot set status to NON DISPONIBILE: you have active tickets!");
                    return "redirect:/user/profile";
                }

                user.setStatus(User.UserStatus.INACTIVE);

            } else if (status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("AVAILABLE")) {
                user.setStatus(User.UserStatus.ACTIVE); // o AVAILABLE se hai questo enum
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated to " + status + "!");
        }

        return "redirect:/user/profile";
    }
}
