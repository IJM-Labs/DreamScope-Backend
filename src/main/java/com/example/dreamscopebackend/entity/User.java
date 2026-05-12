package com.example.dreamscopebackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {


@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "id" , updatable =  false, nullable = false)
private Long id;

public User(){

}

@Column(name = "emailhash" , nullable = false , unique = true)
private String emailHash;

@Column(name = "nickname", nullable = false)
private String nickname;

@Column(name = "terms_accepted", nullable = false)
private boolean termsAccepted = false;

@Column(name = "accepted_at")
private LocalDateTime acceptedAt;

//@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//private List<Dream> dreams = new ArrayList<>();
//
//    // Relation til magic links
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<MagicLink> magicLinks = new ArrayList<>();
//
//    // Relation til user_terms (junction tabel)
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<UserTerms> userTerms = new ArrayList<>();

//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//    }

//    public User(Long id, String emailHash, String nickname, boolean termsAccepted, LocalDateTime acceptedAt, List<Dream> dreams, List<MagicLink> magicLinks, List<UserTerms> userTerms) {
//        this.id = id;
//        this.emailHash = emailHash;
//        this.nickname = nickname;
//        this.termsAccepted = termsAccepted;
//        this.acceptedAt = acceptedAt;
//        this.dreams = dreams;
//        this.magicLinks = magicLinks;
//        this.userTerms = userTerms;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

//    public List<Dream> getDreams() {
//        return dreams;
//    }
//
//    public void setDreams(List<Dream> dreams) {
//        this.dreams = dreams;
//    }
//
//    public List<MagicLink> getMagicLinks() {
//        return magicLinks;
//    }
//
//    public void setMagicLinks(List<MagicLink> magicLinks) {
//        this.magicLinks = magicLinks;
//    }
//
//    public List<UserTerms> getUserTerms() {
//        return userTerms;
//    }
//
//    public void setUserTerms(List<UserTerms> userTerms) {
//        this.userTerms = userTerms;
//    }
}
