package com.example.mimanhwa.Models;

public class ModelUser {

    private String email,name,profileImage,state,userType,userId;

    private  long timestamp;


    public ModelUser(){

    }

    public ModelUser(String email, String name, String profileImage, String state, String userType, String userId, long timestamp) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.state = state;
        this.userType = userType;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
