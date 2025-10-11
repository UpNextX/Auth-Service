package org.upnext.authservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.upnext.authservice.enums.TokenType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String token;


    private Boolean isUsed;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

}
