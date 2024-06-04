import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Member {
    private String name;
    private String number;
    private double MWH;
    private Map<LocalDateTime, Set<String>> MIT = new HashMap<>();
    private Map<LocalDateTime, Set<String>> MET = new HashMap<>();

    public Member(String name, String number) {
        this.name = name;
        this.number = number;
    }
    public String getName() {
        return name;
    }
    public String getNumber() {
        return number;
    }
    public double getMWH() {
        return MWH;
    }
    public Map<LocalDateTime, Set<String>> getMIT() {
        return MIT;
    }
    public Map<LocalDateTime, Set<String>> getMET() {
        return MET;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public void setMWH(double MWH) {
        this.MWH = MWH;
    }
    public void setMIT(Map<LocalDateTime, Set<String>> MIT) {
        this.MIT = MIT;
    }
    public void setMET(Map<LocalDateTime, Set<String>> MET) {
        this.MET = MET;
    }
}