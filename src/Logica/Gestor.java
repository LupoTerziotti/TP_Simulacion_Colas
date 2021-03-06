package Logica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.Fila;
import java.util.ArrayList;


public class Gestor {
    private int caminonesAtendidos;
    private double tiempoPermanencia;
    private int contadorRecalibracion;
    private ObservableList<Fila> data;
    private ArrayList<String> conjuntoEventos;
    private int camionesSinEntrar;
    private LlegadaDeCamion llegadaCamion;
    private FinAtencionRecalibrado finAtRecalibrado;
    private static Evento eventoActual;
    private int ultNumCamion;
    private int dia;
    private int diaDesde;
    private int diaHasta;
    private boolean bool;
    private Recepcion ServidorRecepcion;
    private Balanza ServidorBalanza;
    private ConjuntoDarsena ServidoresDarsena;
    private int diasSimulados;



    public Gestor() {
        this.contadorRecalibracion = 0;
        this.conjuntoEventos = new ArrayList<>();
        this.camionesSinEntrar = 0;
        this.ServidorRecepcion = new Recepcion();
        this.ServidorBalanza = new Balanza();
        this.ServidoresDarsena = new ConjuntoDarsena();
        this.llegadaCamion = new LlegadaDeCamion(getServidorRecepcion());
        this.data = FXCollections.observableArrayList();
        this.diaDesde=0;
        this.diaHasta=31;
        this.dia=1;
        this.diasSimulados=30;

    }

    public Evento getEventoActual() {
        return eventoActual;
    }

    public void setEventoActual(Evento eventoActual) {
        this.eventoActual = eventoActual;
    }

    public int getUltNumCamion() {
        return ultNumCamion;
    }

    public void setUltNumCamion(int ultNumCamion) {
        this.ultNumCamion = ultNumCamion;
    }

    public void inicio() {
        if(getDiaDesde()==0)
        {
            this.cargarFilaPrimeravez(true);
        }
        this.setEventoActual(llegadaCamion);
        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
        Reloj.getInstancia().setTiempoActual(llegadaCamion.getProxLlegadaCamion());
        llegadaCamion.ejecutar();
        this.llegadaCamion.setCamion(llegadaCamion.generarCamion());
        if(getDiaDesde()==0)
        {
            this.cargarFila(true);
        }
        this.llegadaCamion.sumarContadorCamiones();
        iterar();
    }

    public void iterar() {


        int aux = 0;
        for ( int i=0; i < 31; i++) { //30 dias

            if(i==0)
            {
                this.cargarFila(bool);
            }
            if(i>0){
                aux=aux+24;
                setDia(i);
                this.cargarFilaPrimeravez(bool);
            }
            ;

            while ((Reloj.getInstancia().getTiempoActual()/3600) < 18+aux) { //Esta seria la hora de cierre (18hs)
                if((i)>=getDiaDesde() && (i)<=getDiaHasta()){
                    bool=true;
                }else
                {
                    bool=false;
                }

                switch (proxEvento()) {
                    case "Recepcion":
                        FinAtencionRecepcion finAtRecepcion = new FinAtencionRecepcion(this.ServidorRecepcion, this.ServidorBalanza);
                        this.setEventoActual(finAtRecepcion);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        this.tiempoPermanencia += getServidorRecepcion().getTiempoAtencion();
                        getServidorRecepcion().getCamion().setTiempoFinAtRecepcion(getServidorRecepcion().getProxFinAtencion());
                        Reloj.getInstancia().setTiempoActual(this.getServidorRecepcion().getProxFinAtencion());
                        finAtRecepcion.ejecutar();
                        if (getServidorRecepcion().getCamion() == null) {
                            getServidorRecepcion().setRandomAtencion(0);
                            getServidorRecepcion().setTiempoAtencion(0);
                            getServidorRecepcion().setProxFinAtencion(0);
                        }
                        this.cargarFila(bool);
                        break;
                    case "Balanza":
                        FinAtencionBalanza finAtBalanza = new FinAtencionBalanza(this.ServidorBalanza, this.ServidoresDarsena);
                        this.setEventoActual(finAtBalanza);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        this.tiempoPermanencia += getServidorBalanza().getTiempoAtencion();
                        getServidorBalanza().getCamion().setTiempoFinAtBalanza(getServidorBalanza().getProxFinAtencion());
                        Reloj.getInstancia().setTiempoActual(this.getServidorBalanza().getProxFinAtencion());
                        finAtBalanza.ejecutar();
                        if (getServidorBalanza().getCamion() == null) {
                            getServidorBalanza().setRandomAtencion(0);
                            getServidorBalanza().setTiempoAtencion(0);
                            getServidorBalanza().setProxFinAtencion(0);
                        }
                        this.cargarFila(bool);
                        break;
                    case "Darsena":
                        Darsena darsenaFinalizada = this.ServidoresDarsena.getUltimaDarsena();
                        FinAtencionDarsena finAtDarsena = new FinAtencionDarsena(this.ServidoresDarsena, darsenaFinalizada.getId() - 1);
                        this.setEventoActual(finAtDarsena);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        this.tiempoPermanencia += getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).getTiempoAtencion();
                        Reloj.getInstancia().setTiempoActual(this.getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).getProxFinAtencion());
                        finAtDarsena.ejecutar();
                        this.caminonesAtendidos++;
                        contadorRecalibracion++;
                        if (contadorRecalibracion == 15) {
                            setContadorRecalibracion(0);
                            ServidoresDarsena.getDarsena(darsenaFinalizada.getId() - 1).calcularTiempoRecalibrado();
                            this.finAtRecalibrado = new FinAtencionRecalibrado(ServidoresDarsena, darsenaFinalizada.getId() - 1);
                            this.cargarFila(bool);
                            break;
                        }
                        if (getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).getCamion() == null) {
                            getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).setRandomAtencion(0);
                            getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).setTiempoAtencion(0);
                            getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).setProxFinAtencion(0);
                        }
                        this.cargarFila(bool);
                        break;

                    case "LlegadaCamion":
                        this.setEventoActual(llegadaCamion);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        Reloj.getInstancia().setTiempoActual(llegadaCamion.getProxLlegadaCamion());
                        this.setUltNumCamion(llegadaCamion.getCamion().getNumero());
                        llegadaCamion.ejecutar();
                        this.llegadaCamion.setCamion(llegadaCamion.generarCamion());
                        llegadaCamion.sumarContadorCamiones();
                        this.cargarFila(bool);
                        break;

                    case "FinRecalibracion":
                        this.setEventoActual(finAtRecalibrado);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        Reloj.getInstancia().setTiempoActual(finAtRecalibrado.getDarsenaFinalizada().getProxFinRecalibrado());
                        this.finAtRecalibrado.ejecutar();
                        this.finAtRecalibrado.getDarsenaFinalizada().setTiempoRecalibrado(0);
                        this.finAtRecalibrado.getDarsenaFinalizada().setProxFinRecalibrado(0);
                        this.cargarFila(bool);
                        break;

                }
            }

            while ((Reloj.getInstancia().getTiempoActual()/(3600)) < 36+aux) {//Son las 5AM pasado para el otro dia (Hora 29)
                switch (proxEvento()) {
                    case "Recepcion":
                        this.cargarFila(bool);
                        FinAtencionRecepcion finAtRecepcion = new FinAtencionRecepcion(this.ServidorRecepcion, this.ServidorBalanza);
                        this.setEventoActual(finAtRecepcion);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        this.tiempoPermanencia += getServidorRecepcion().getTiempoAtencion();
                        this.camionesSinEntrar+=this.getServidorRecepcion().getCola().size();
                        Reloj.getInstancia().setTiempoActual(this.getServidorRecepcion().getProxFinAtencion());
                        finAtRecepcion.ejecutarFueraDeHora();
                        if (getServidorRecepcion().getCamion() == null) {
                            getServidorRecepcion().setRandomAtencion(0);
                            getServidorRecepcion().setTiempoAtencion(0);
                            getServidorRecepcion().setProxFinAtencion(0);
                        }
                        this.cargarFila(bool);
                        break;
                    case "Balanza":
                        FinAtencionBalanza finAtBalanza = new FinAtencionBalanza(this.ServidorBalanza, this.ServidoresDarsena);
                        this.setEventoActual(finAtBalanza);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        this.tiempoPermanencia += getServidorBalanza().getTiempoAtencion();
                        Reloj.getInstancia().setTiempoActual(this.getServidorBalanza().getProxFinAtencion());
                        finAtBalanza.ejecutar();
                        if (getServidorBalanza().getCamion() == null) {
                            getServidorBalanza().setRandomAtencion(0);
                            getServidorBalanza().setTiempoAtencion(0);
                            getServidorBalanza().setProxFinAtencion(0);
                        }
                        this.cargarFila(bool);
                        break;
                    case "Darsena":
                        Darsena darsenaFinalizada = this.ServidoresDarsena.getUltimaDarsena();
                        FinAtencionDarsena finAtDarsena = new FinAtencionDarsena(this.ServidoresDarsena, darsenaFinalizada.getId() - 1);
                        this.setEventoActual(finAtDarsena);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        this.tiempoPermanencia += getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).getTiempoAtencion();
                        Reloj.getInstancia().setTiempoActual(this.getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).getProxFinAtencion());
                        finAtDarsena.ejecutar();
                        this.caminonesAtendidos++;
                        contadorRecalibracion++;
                        if (contadorRecalibracion == 15) {
                            setContadorRecalibracion(0);
                            ServidoresDarsena.getDarsena(darsenaFinalizada.getId() - 1).calcularTiempoRecalibrado();
                            this.finAtRecalibrado = new FinAtencionRecalibrado(ServidoresDarsena, darsenaFinalizada.getId() - 1);
                            this.cargarFila(bool);
                            break;
                        }
                        if (getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).getCamion() == null) {
                            getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).setRandomAtencion(0);
                            getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).setTiempoAtencion(0);
                            getServidoresDarsena().getDarsena(darsenaFinalizada.getId() - 1).setProxFinAtencion(0);
                        }
                        this.cargarFila(bool);
                        break;

                    case "LlegadaCamion": //La diferencia es que ahora no se despachan camiones a recepcion

                        this.setEventoActual(llegadaCamion);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre() + " Fuera de hora");
                        Reloj.getInstancia().setTiempoActual(llegadaCamion.getProxLlegadaCamion());
                        llegadaCamion.setCamion(null);
                        this.llegadaCamion.setCamion(llegadaCamion.generarCamionFueraHora());
                        break;

                    case "FinRecalibracion":
                        this.cargarFila(bool);
                        this.setEventoActual(finAtRecalibrado);
                        this.getConjuntoEventos().add(this.getEventoActual().getNombre());
                        Reloj.getInstancia().setTiempoActual(finAtRecalibrado.getDarsenaFinalizada().getProxFinRecalibrado());
                        this.finAtRecalibrado.ejecutar();
                        this.finAtRecalibrado.getDarsenaFinalizada().setTiempoRecalibrado(0);
                        this.finAtRecalibrado.getDarsenaFinalizada().setProxFinRecalibrado(0);
                        this.cargarFila(bool);
                        break;
                }
            }
        }
    }


    public double tiempoMinimo() {
        double minTiempo = 25920000;   //seteo el tiempo minimo en un valor bien alto para que pueda funcionar
        if (ServidoresDarsena.getDarsenas()[0].getProxFinAtencion() != 0) {
            minTiempo = ServidoresDarsena.getDarsenas()[0].getProxFinAtencion();
        }
        if (ServidoresDarsena.getDarsenas()[1].getProxFinAtencion() != 0 && ServidoresDarsena.getDarsenas()[1].getProxFinAtencion() < minTiempo) {
            minTiempo = ServidoresDarsena.getDarsenas()[1].getProxFinAtencion();
        }
        if (ServidorBalanza.getProxFinAtencion() != 0 && ServidorBalanza.getProxFinAtencion() < minTiempo) {
            minTiempo = ServidorBalanza.getProxFinAtencion();
        }
        if (ServidorRecepcion.getProxFinAtencion() != 0 && ServidorRecepcion.getProxFinAtencion() < minTiempo) {
            minTiempo = ServidorRecepcion.getProxFinAtencion();
        }
        if (llegadaCamion.getProxLlegadaCamion() != 0 && llegadaCamion.getProxLlegadaCamion() < minTiempo) {
            minTiempo = llegadaCamion.getProxLlegadaCamion();
        }
        if (ServidoresDarsena.getDarsenas()[0].getProxFinRecalibrado() != 0 && ServidoresDarsena.getDarsenas()[0].getProxFinRecalibrado() < minTiempo) {
            minTiempo = ServidoresDarsena.getDarsenas()[0].getProxFinRecalibrado();
        }
        if (ServidoresDarsena.getDarsenas()[1].getProxFinRecalibrado() != 0 && ServidoresDarsena.getDarsenas()[1].getProxFinRecalibrado() < minTiempo) {
            minTiempo = ServidoresDarsena.getDarsenas()[1].getProxFinRecalibrado();
        }
        return minTiempo;
    }

    public String proxEvento() {
        double tiempo = tiempoMinimo();

        if (tiempo == ServidoresDarsena.getDarsenas()[0].getProxFinAtencion()) {
            return "Darsena";
        } else if (tiempo == ServidoresDarsena.getDarsenas()[1].getProxFinAtencion()) {
            return "Darsena";
        } else if (tiempo == ServidorBalanza.getProxFinAtencion()) {
            return "Balanza";
        } else if (tiempo == llegadaCamion.getProxLlegadaCamion()) {
            return "LlegadaCamion";
        } else if (tiempo == ServidoresDarsena.getDarsenas()[0].getProxFinRecalibrado()) {
            return "FinRecalibracion";
        } else if (tiempo == ServidoresDarsena.getDarsenas()[1].getProxFinRecalibrado()) {
            return "FinRecalibracion";
        } else {
            return "Recepcion";
        }
    }

    public ObservableList<Fila> getData() {
        return this.data;
    }

    public void cargarFila(boolean bool) {
        if(bool)
        {
            String diaContent=Integer.toString(getDia());
            String relojContent = Reloj.getInstancia().tiempoString();
            String eventContent = eventoActual.getNombre();
            String colaEnPuertaContent = Integer.toString(this.camionesSinEntrar);
            String camionContent = (llegadaCamion.getCamion() != null) ? llegadaCamion.getCamion().getNumeroString() : "-";
            String rnd1Content = Double.toString(llegadaCamion.getRandomLlegada());
            String tiempoEntreLlegadasContent = llegadaCamion.getTiempoLlegada1();
            String proxContent = llegadaCamion.getProxFinLlegada1();
            String colaRecepcionContent = Integer.toString(getServidorRecepcion().getCola().size());
            String camionRecepcionContent = (getServidorRecepcion().getCamion() != null) ? getServidorRecepcion().getCamion().getNumeroString() : "-";
            String estadoRecepcionContet = getServidorRecepcion().getEstado().getName();
            String rndRecepcionContent = Double.toString(getServidorRecepcion().getRandomAtencion());
            String tiempoLlegadaRecepcionContent = getServidorRecepcion().getTiempoAtencion1();
            String proxFinAtencionRecepcionContent = getServidorRecepcion().getProxFinAtencion1();
            String colaBalanzaContent = Integer.toString(getServidorBalanza().getCola().size());
            String camionBalanzContent = (getServidorBalanza().getCamion() != null) ? getServidorBalanza().getCamion().getNumeroString() : "-";
            String estadBalanzContent = getServidorBalanza().getEstadoBalanza().getName();
            String rndBalanzaContent = Double.toString(getServidorBalanza().getRandomAtencion());
            String tiempoAtencionBalanzContent = getServidorBalanza().tiempoAtencion1();
            String proxFinAtBalContent = getServidorBalanza().proxFinTiempoAtencion1();
            String camionDarse1Content = (getServidoresDarsena().getDarsenas()[0].getCamion() != null) ? getServidoresDarsena().getDarsenas()[0].getCamion().getNumeroString() : "-";
            String estadoDarse1Content = getServidoresDarsena().getDarsenas()[0].getEstadoDarsena().getName();
            String rndDarsen1Content = Double.toString(getServidoresDarsena().getDarsenas()[0].getRandomAtencion());
            String tiempoAtencionDarse1Content = getServidoresDarsena().getDarsenas()[0].getTiempoAtencion1();
            String finAtencionProxDarse1Content = getServidoresDarsena().getDarsenas()[0].getProxFinAtencion1();
            String camionDarse2Content = (getServidoresDarsena().getDarsenas()[1].getCamion() != null) ? getServidoresDarsena().getDarsenas()[1].getCamion().getNumeroString() : "-";
            String estadoDarse2Content = getServidoresDarsena().getDarsenas()[1].getEstadoDarsena().getName();
            String rndDarse2Content = Double.toString(getServidoresDarsena().getDarsenas()[1].getRandomAtencion());
            String tiempoArDarse2Content = getServidoresDarsena().getDarsenas()[1].getTiempoAtencion1();
            String proxFinAtDarse2Content = getServidoresDarsena().getDarsenas()[1].getProxFinAtencion1();
            String colaDarsenaContent = Integer.toString(getServidoresDarsena().getCola().size());

            data.add(new Fila(diaContent, relojContent, eventContent, colaEnPuertaContent,camionContent, rnd1Content, tiempoEntreLlegadasContent, proxContent, colaRecepcionContent,
                    camionRecepcionContent, estadoRecepcionContet, rndRecepcionContent, tiempoLlegadaRecepcionContent, proxFinAtencionRecepcionContent,
                    colaBalanzaContent, camionBalanzContent, estadBalanzContent, rndBalanzaContent, tiempoAtencionBalanzContent, proxFinAtBalContent,
                    camionDarse1Content, estadoDarse1Content, rndDarsen1Content, tiempoAtencionDarse1Content, finAtencionProxDarse1Content,
                    camionDarse2Content, estadoDarse2Content, rndDarse2Content, tiempoArDarse2Content, proxFinAtDarse2Content, colaDarsenaContent));
        }


    }

    public void cargarFilaPrimeravez(boolean bool) {

        if(bool) {
            String diaContent = String.valueOf(getDia());
            String relojContent = "05:00:00";
            String eventContent = "Inicio del dia";
            String camionContent = "-";
            String rnd1Content = "-";
            String tiempoEntreLlegadasContent = "00:00:00";
            String proxContent = "00:00:00";
            String colaEnPuertaContent = "0";
            String camionRecepcionContent = "-";
            String estadoRecepcionContet = "Libre";
            String rndRecepcionContent = "-";
            String tiempoLlegadaRecepcionContent = "00:00:00";
            String proxFinAtencionRecepcionContent = "00:00:00";
            String colaRecepcionContent = "0";
            String camionBalanzContent = "-";
            String estadBalanzContent = "Libre";
            String rndBalanzaContent = "-";
            String tiempoAtencionBalanzContent = "00:00:00";
            String proxFinAtBalContent = "00:00:00";
            String camionDarse1Content = "-";
            String estadoDarse1Content = "Libre";
            String rndDarsen1Content = "-";
            String tiempoAtencionDarse1Content = "00:00:00";
            String finAtencionProxDarse1Content = "00:00:00";
            String camionDarse2Content = "-";
            String estadoDarse2Content = "Libre";
            String rndDarse2Content = "-";
            String tiempoArDarse2Content = "00:00:00";
            String proxFinAtDarse2Content = "00:00:00";
            String colaDarsenaContent = "0";

            data.add(new Fila(diaContent, relojContent, eventContent, colaEnPuertaContent, camionContent, rnd1Content, tiempoEntreLlegadasContent, proxContent, colaRecepcionContent, camionRecepcionContent, estadoRecepcionContet, rndRecepcionContent, tiempoLlegadaRecepcionContent, proxFinAtencionRecepcionContent, colaRecepcionContent, camionBalanzContent, estadBalanzContent, rndBalanzaContent, tiempoAtencionBalanzContent, proxFinAtBalContent, camionDarse1Content, estadoDarse1Content, rndDarsen1Content, tiempoAtencionDarse1Content, finAtencionProxDarse1Content, camionDarse2Content, estadoDarse2Content, rndDarse2Content, tiempoArDarse2Content, proxFinAtDarse2Content, colaDarsenaContent));
        }
    }

    public String promedioDeCamionesAtendidosPorDia() {
        return String.valueOf((caminonesAtendidos/diasSimulados));
    }

    public String cantidadDeCamionesNoAtendidos(){
        return String.valueOf(getCamionesSinEntrar());
    }

    public String promedioDeTiempoDePermanencia() {
        return Reloj.tiempoString(tiempoPermanencia/caminonesAtendidos);
    }

    public  String cantidadDeCamionesTotales()
    {
        return String.valueOf(this.caminonesAtendidos);
    }


    public Recepcion getServidorRecepcion() {
        return ServidorRecepcion;
    }

    public void setServidorRecepcion(Recepcion servidorRecepcion) {
        ServidorRecepcion = servidorRecepcion;
    }

    public Balanza getServidorBalanza() {
        return ServidorBalanza;
    }

    public void setServidorBalanza(Balanza servidorBalanza) {
        ServidorBalanza = servidorBalanza;
    }

    public ConjuntoDarsena getServidoresDarsena() {
        return ServidoresDarsena;
    }

    public void setServidoresDarsena(ConjuntoDarsena servidoresDarsena) {
        ServidoresDarsena = servidoresDarsena;
    }
    public int getDiaHasta() {
        return diaHasta;
    }

    public void setDiaHasta(int diaHasta) {
        this.diaHasta = diaHasta;
    }

    public int getDiaDesde() {
        return diaDesde;
    }

    public void setDiaDesde(int diaDesde) {
        this.diaDesde = diaDesde;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getContadorRecalibracion() {
        return contadorRecalibracion;
    }

    public void setContadorRecalibracion(int contador) {
        this.contadorRecalibracion = contador;
    }


    public ArrayList<String> getConjuntoEventos() {
        return conjuntoEventos;
    }

    public void setConjuntoEventos(ArrayList<String> conjuntoEventos) {
        this.conjuntoEventos = conjuntoEventos;
    }

    public int getCamionesSinEntrar() {
        return camionesSinEntrar;
    }

    public void setCamionesSinEntrar(int camionesSinEntrar) {
        this.camionesSinEntrar = camionesSinEntrar;
    }
}
