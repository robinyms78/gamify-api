package sg.edu.ntu.gamify_demo.dtos;

import sg.edu.ntu.gamify_demo.models.User;

public record AuthResponse(
    String token,
    User user
) {}
