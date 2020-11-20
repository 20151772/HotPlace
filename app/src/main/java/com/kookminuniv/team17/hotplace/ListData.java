// ListData
// 기능 : 리스트뷰에 넣을 데이터 타입
// 개발 : 고동훈, 김명호(파이어베이스 적용을 위해 구조 변경)

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
