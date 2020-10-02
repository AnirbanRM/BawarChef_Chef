package com.bawarchef.Containers;

import java.io.Serializable;

public class UserIdentity implements Serializable{

    public class Address implements Serializable {
        public String address="";
        public String city = "";
        public String state="";
        public String pinNo="";
    }

    public UserIdentity(){}

    public String userID,fname,lname,mob,email;
    public Address addr = new Address();
}
