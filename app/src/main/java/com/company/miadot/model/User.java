package com.company.miadot.model;

public class User {
    private String fullName;
    private String nickname;
    private String email;
    private String photoUrl;
    private String cep;
    private String street;
    private String neighborhood;
    private String city;
    private String state;

    public User() {
        // Construtor vazio necessário para Firebase
    }

    public User(String fullName, String nickname, String email, String photoUrl,
                String cep, String street, String neighborhood, String city, String state) {
        this.fullName = fullName;
        this.nickname = nickname;
        this.email = email;
        this.photoUrl = photoUrl;
        this.cep = cep;
        this.street = street;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
    }

    // Getters e Setters

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
