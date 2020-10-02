package com.bawarchef.Containers;

import java.io.Serializable;

public class ChefIdentity implements Serializable {

    public class Address implements Serializable{
        public String address="";
        public String city = "";
        public String state="";
        public String pinNo="";
    }

    public ChefIdentity(){}

    public String regNo,fname,lname,mob,email,altmob,emermob,dob,aadhar;
    public Address resAddr = new Address(),mailAddr = new Address();
    public char gender = 'M';
}
