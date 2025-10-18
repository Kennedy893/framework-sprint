package controller;

import annotation.UrlAnnotation;

public class UrlController 
{

    @UrlAnnotation("/home")
    public void home() 
    {
        System.out.println("Methode home() appelee !");
    }

    @UrlAnnotation("/user")
    public void user() 
    {
        System.out.println("Methode user() appelee !");
    }
}
