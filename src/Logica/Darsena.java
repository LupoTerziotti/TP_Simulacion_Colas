package Logica;

import java.util.LinkedList;

public class Darsena {
    private int id;
    private long tiempoAtencion;
    private long proxFinAtencion;
    private double randomAtencion;
    private Camion camion;
    private EstadoDarsena estadoDarsena;

    public Darsena() {
        this.estadoDarsena = EstadoDarsena.Libre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRandomAtencion() {
        return randomAtencion;
    }

    public void setRandomAtencion(double randomAtencion) {
        this.randomAtencion = randomAtencion;
    }

    public long getTiempoAtencion() {
        return tiempoAtencion;
    }

    public void setTiempoAtencion(long tiempoAtencion) {
        this.tiempoAtencion = tiempoAtencion;
    }

    public long getProxFinAtencion() {
        return proxFinAtencion;
    }

    public void setProxFinAtencion(long proxFinAtencion) {
        this.proxFinAtencion = proxFinAtencion;
    }

    public Camion getCamion() {
        return camion;
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
        if(camion!=null){
            calcularTiempoAtencion();
            proximoDarsena(Reloj.getInstancia().getTiempoActual());
        }
        else{
            this.tiempoAtencion = 0;
            this.proxFinAtencion = 0;
        }
    }

    public EstadoDarsena getEstadoDarsena() {
        return estadoDarsena;
    }

    public void setEstadoDarsena(EstadoDarsena estadoDarsena) {
        this.estadoDarsena = estadoDarsena;
    }

    @Override
    public String toString() {
        return "Darsena ("+ id +") {" + "estado=" + this.getEstadoDarsena() + '}';
    }

    //Calcula el tiempo de atencion
    public void calcularTiempoAtencion() {
        this.setRandomAtencion(Math.random());
        double demora = 15 + this.getRandomAtencion() * 5;
        this.tiempoAtencion = (long) (demora / 60);
    }

    //Calcula el proximo fin de atencion
    public void proximoDarsena(long relojActual) {
        this.proxFinAtencion = relojActual + this.tiempoAtencion;
    }
}
