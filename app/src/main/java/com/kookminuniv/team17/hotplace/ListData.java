package com.kookminuniv.team17.hotplace;

public class ListData {
    private String imageName;
    private String titleStr;
    private String descStr;

    public void setImageName(String imageName){
        this.imageName = imageName;
    }
    public void setTitle(String title){
        titleStr = title;
    }
    public void setDesc(String desc){
        descStr = desc;
    }

    public String getImageName(){
        return this.imageName;
    }
    public String getTitle(){
        return this.titleStr;
    }
    public String getDesc(){
        return this.descStr;
    }
}
