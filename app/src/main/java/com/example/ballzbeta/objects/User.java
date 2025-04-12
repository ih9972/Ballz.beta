package com.example.ballzbeta.objects;

public class User {
    private String uid;
    private String name;
    private int role;
    private String  companyId;

    public User (){
        uid = "-1";
        name = null;
        role = -1;
    }

    public User (String uid,String name,int role, String  companyId){
        this.uid = uid;
        this.name = name;
        this.role = role;
        this.companyId = companyId;

    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}

