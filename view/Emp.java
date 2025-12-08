package view;

public class Emp 
{
    private String nom;
    private int age;
    private double salaire;

    public Emp(){}

    public Emp(String nom, int age, double salaire) {
        this.nom = nom;
        this.age = age;
        this.salaire = salaire;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public double getSalaire() {
        return salaire;
    }
    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }    
}
