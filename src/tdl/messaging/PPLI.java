/*
 * This class handle the PPLI object
 * Created by Wichai Pawgasame (ODC3)
 * Date created: 20/06/2014
 * Last modified: 20/05/2015
 */

package tdl.messaging;

/**
 *
 * @author wichai.p
 */
public class PPLI {
    
    public String posId;
    public String posName;
    public double posLat;
    public double posLon;
    public double speed;
    public double trueCourse;
    public double magVariation;
    public String posTime;
    public String posDate;
    
    public PPLI() {
        
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    
    public double getPosLat() {
        return posLat;
    }

    public void setPosLat(double posLat) {
        this.posLat = posLat;
    }

    public double getPosLon() {
        return posLon;
    }

    public void setPosLon(double posLon) {
        this.posLon = posLon;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getTrueCourse() {
        return trueCourse;
    }

    public void setTrueCourse(double trueCourse) {
        this.trueCourse = trueCourse;
    }

    public double getMagVariation() {
        return magVariation;
    }

    public void setMagVariation(double magVariation) {
        this.magVariation = magVariation;
    }

    public String getPosTime() {
        return posTime;
    }

    public void setPosTime(String posTime) {
        this.posTime = posTime;
    }

    public String getPosDate() {
        return posDate;
    }

    public void setPosDate(String posDate) {
        this.posDate = posDate;
    }
    
    
    
    
}
