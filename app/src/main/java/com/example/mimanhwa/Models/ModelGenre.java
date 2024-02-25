package com.example.mimanhwa.Models;

public class ModelGenre {
    String id,genre,uid,url,dirImg;
    long timestamp;

    public  ModelGenre(){

    }

    public  ModelGenre(String id,String genre,String uid,String url,String dirImg, long timestamp){

        this.id = id;
        this.genre = genre;
        this.uid = uid;
        this.timestamp = timestamp;
        this.url = url;
        this.dirImg = dirImg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String category) {
        this.genre = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDirImg() {
        return dirImg;
    }

    public void setDirImg(String dirImg) {
        this.dirImg = dirImg;
    }
}
