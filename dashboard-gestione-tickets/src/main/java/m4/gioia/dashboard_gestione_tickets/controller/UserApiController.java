package m4.gioia.dashboard_gestione_tickets.controller;

import com.esercizio.milestone.ticket_platform.model.Ticket;
import com.esercizio.milestone.ticket_platform.model.User;
import com.esercizio.milestone.ticket_platform.repository.TicketRepository;
import com.esercizio.milestone.ticket_platform.repository.UserRepository;
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

import java.util.Optional;

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
        boolean canUpdateStatus = ticketRepository
                .findTicketByStatusAndUser_Username(Ticket.TicketStatus.TO_DO, username).size()
                + ticketRepository.findTicketByStatusAndUser_Username(Ticket.TicketStatus.IN_PROGRESS, username)
                        .size() == 0;
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("userName", user.getName());
            model.addAttribute("userSurname", user.getSurname());
            model.addAttribute("userStatus", user.getStatus());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("canUpdateStatus", canUpdateStatus);
        }
        return "/user/profile";
    }

    @PostMapping("/profile/update-status")
    public String updateStatus(@RequestParam("status") String status, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (status.equals("Available")) {
                user.setStatus(User.UserStatus.AVAILABLE);
            } else if (status.equals("Inactive")) {
                user.setStatus(User.UserStatus.NOT_AVAILABLE);
            }

            userRepository.save(user);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Status updated in " + status + "!");
        return "redirect:/user/profile";
    }
}
