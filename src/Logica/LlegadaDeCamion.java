package Logica;

public class LlegadaDeCamion extends Evento
{

    private long tiempoLlegada;
    private Camion camion;
    private double randomLlegada;
    private Recepcion recepcion;
    private static int contadorCamiones = 1;

    public long getTiempoLlegada() {
        return tiempoLlegada;
    }

    public void setTiempoLlegada(long tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }

    public Camion getCamion() {
        return camion;
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
    }

    public double getRandomLlegada() {
        return randomLlegada;
    }

    public void setRandomLlegada(double randomLlegada) {
        this.randomLlegada = randomLlegada;
    }

    public LlegadaDeCamion(long tiempoLlegada, Gestor gestor, Recepcion recepcion)
    {
        this.gestor=gestor;
        this.randomLlegada=Math.random();
        this.tiempoLlegada=tiempoLlegada;
        this.recepcion=recepcion;
    };

    public LlegadaDeCamion(Gestor gestor) {
        this.gestor=gestor;
        this.camion = generarCamion();
        this.randomLlegada = Math.random();
        this.tiempoLlegada=calcularTiempoLlegada(gestor.getReloj());
        this.recepcion = new Recepcion();
        contadorCamiones++;

    }

    public String getNombre()
    {
        String nombre= "Lleg Camion";
        return nombre;
    }

    public long calcularTiempoLlegada(long tiempoActual)
    {

        return Distribuciones.proximoRecepcion(tiempoActual,randomLlegada);
    }

    public Camion generarCamion()
    {
        Camion camion = new Camion(contadorCamiones,EstadoCamion.Nuevo);
        camion.setNumero(contadorCamiones);
        return camion;
    }


    public void asignarRecepcion(Camion c)
    {
        if(recepcion.getCola()==null && recepcion.getEstado()==EstadoRecepcion.Libre)
        {

                recepcion.setCamion(c);
                c.setEstado(EstadoCamion.En_Recepcion);
                recepcion.setEstado(EstadoRecepcion.Ocupado);
        }
        else
        {
            c.setEstado(EstadoCamion.En_cola_Recepcion);
            recepcion.agregarCamionAcola(c);
        }
    }

    public Recepcion getRecepcion() {
        return recepcion;
    }

    public void setRecepcion(Recepcion recepcion) {
        this.recepcion = recepcion;
    }

    public static int getContadorCamiones() {
        return contadorCamiones;
    }

    public static void setContadorCamiones(int contadorCamiones) {
        LlegadaDeCamion.contadorCamiones = contadorCamiones;
    }

    public void ejecutar()
    {

        LlegadaDeCamion proximaLlegada=new LlegadaDeCamion(this.gestor);
        gestor.addCamion(camion);
        gestor.getConjuntoEventos().add(proximaLlegada);
        asignarRecepcion(camion);
        FinAtencionRecepcion finAtencion= new FinAtencionRecepcion(gestor.getReloj(),gestor,getRecepcion());

        finAtencion.ejecutar();
    }
}
