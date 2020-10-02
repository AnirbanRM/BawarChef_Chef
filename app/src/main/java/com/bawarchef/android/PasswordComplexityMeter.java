package com.bawarchef.android;

public class PasswordComplexityMeter {

    String str;

    PasswordComplexityMeter(String str){
        this.str = str;
    }

    int getStrength(){
        if(str.length()==0)return -1;
        int score = positives()+negatives();
        if(score<0)return 0;
        else if(score>100)return 100;
        return score;
    }

    public void setString(String str){
        this.str = str;
    }

    public String getString(){
        return str;
    }

    private int negatives() {
        return -(lettersOnly()+numbersOnly()+repChar()+consecLowerCase()+consecNumbers()+consecUpperCase());
    }

    private int positives() {
        return noOfChar()+upperCase()+lowerCase()+numbers()+symbols()+symNumBet()+req();
    }

    private int noOfChar(){
        return str.length()*4;
    }
    private int upperCase(){
        int count=0;
        for(char i : str.toCharArray()){
            if(i>='A'&&i<='Z')
                count++;
        }
        return (str.length()-count)*2;
    }
    private int lowerCase(){
        int count=0;
        for(char i : str.toCharArray()){
            if(i>='a'&&i<='z')
                count++;
        }
        return (str.length()-count)*2;
    }
    private int numbers(){
        int count=0;
        for(char i : str.toCharArray()){
            if(i>='0'&&i<='9')
                count++;
        }
        return count*4;
    }
    private int symbols(){
        int count=0;
        for(char i : str.toCharArray()){
            if((i>='0'&&i<='9')||(i>='a'&&i<='z')||(i>='A'&&i<='Z'));
            else
                count++;
        }
        return count*6;
    }
    private int symNumBet(){
        int count = 0;
        for(int j=1; j<str.length()-1;j++) {
            char i=str.charAt(j);
            if((i>='a'&&i<='z')||(i>='A'&&i<='Z'));
            else
                count++;
        }
        return count*2;
    }
    private int req(){
        int sc = 0;
        if(str.length()>=8)sc++;
        for(char c : str.toCharArray()){
            if(c>='A'&&c<='Z'){
                sc++;break;
            }
        }
        for(char c : str.toCharArray()){
            if(c>='a'&&c<='z'){
                sc++;break;
            }
        }
        for(char c : str.toCharArray()){
            if(c>='0'&&c<='9'){
                sc++;break;
            }
        }
        for(char c : str.toCharArray()){
            if((c>='0'&&c<='9')||(c>='a'&&c<='z')||(c>='A'&&c<='Z'));
            else {
                sc++;break;
            }
        }
        if(sc==5)return 10;
        else
            return 0;
    }

    private int lettersOnly(){
        int count = 0;
        for(char c : str.toCharArray()){
            if((c>='A'&&c<='Z')||(c>='a'&&c<='z'))
                count++;
        }
        if(str.length()==count)return count;
        else return 0;
    }

    private int numbersOnly(){
        int count = 0;
        for(char c : str.toCharArray()){
            if(c>='0'&&c<='9')
                count++;
        }
        if(str.length()==count)return count;
        else return 0;
    }

    private int repChar(){
        int count = 0,repChar=0;
        for (int a=0; a < str.length(); a++) {
            boolean charExists = false;
            for (int b=0; b < str.length(); b++) {
                if (str.charAt(a) == str.charAt(b) && a != b) {
                    charExists = true;
                    count += Math.abs(str.length()/(b-a));
                }
            }
            if (charExists) {
                repChar++;
                int unique =str.length()-repChar;
                if(unique!=0)
                    count/=unique;
            }
        }
        return count;
    }

    private int consecUpperCase(){
        int count = 0;
        for(int i =0; i<str.length()-1; i++){
            char f = str.charAt(i);
            char s = str.charAt(i+1);
            if(f>='A'&&f<='Z'&&s>='A'&&s<='Z')
                count++;
        }
        return count*2;
    }

    private int consecLowerCase(){
        int count = 0;
        for(int i =0; i<str.length()-1; i++){
            char f = str.charAt(i);
            char s = str.charAt(i+1);
            if(f>='a'&&f<='z'&&s>='a'&&s<='z')
                count++;
        }
        return count*2;
    }

    private int consecNumbers(){
        int count = 0;
        for(int i =0; i<str.length()-1; i++){
            char f = str.charAt(i);
            char s = str.charAt(i+1);
            if(f>='0'&&f<='9'&&s>='0'&&s<='9')
                count++;
        }
        return count*2;
    }



}
