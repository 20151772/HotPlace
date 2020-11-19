package com.kookminuniv.team17.hotplace;

public class ListData {
    private String imageName;
    private String articleNumber;
    private String title;

    public ListData(String imageName, String articleNumber, String title){
        this.imageName = imageName;
        this.articleNumber = articleNumber;
        this.title = title;
    }

    public void setImageName(String imageName){
        this.imageName = imageName;
    }
    public void setArticleNumbertArticle(String title){
        articleNumber = title;
    }
    public void setTitle(String desc){
        title = desc;
    }

    public String getImageName(){
        return this.imageName;
    }
    public String getArticleNumber(){
        return this.articleNumber;
    }
    public String getTitle(){
        return this.title;
    }
}
