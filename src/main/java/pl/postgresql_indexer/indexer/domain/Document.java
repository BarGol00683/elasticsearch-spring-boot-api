package pl.postgresql_indexer.indexer.domain;

import lombok.Data;

@Data
public class Document {

    private String trip_id;

    private String year;

    private String month;

    private String week;

    private String day;

    private String hour;

    private String usertype;

    private String gender;

    private String starttime;

    private String stoptime;

    private String tripduration;

    private String temperature;

    private String events;

    private String from_station_id;

    private String from_station_name;

    private String latitude_start;

    private String longitude_start;

    private String dpcapacity_start;

    private String to_station_id;

    private String to_station_name;

    private String latitude_end;

    private String longitude_end;

    private String dpcapacity_end;

    public Document() {

    }

    public Document(String[] inputs) {
        this.trip_id = inputs[0];
        this.year = inputs[1];
        this.month = inputs[2];
        this.week = inputs[3];
        this.day = inputs[4];
        this.hour = inputs[5];
        this.usertype = inputs[6];
        this.gender = inputs[7];
        this.starttime = inputs[8];
        this.stoptime = inputs[9];
        this.tripduration = inputs[10];
        this.temperature = inputs[11];
        this.events = inputs[12];
        this.from_station_id = inputs[13];
        this.from_station_name = inputs[14];
        this.latitude_start = inputs[15];
        this.longitude_start = inputs[16];
        this.dpcapacity_start = inputs[17];
        this.to_station_id = inputs[18];
        this.to_station_name = inputs[19];
        this.latitude_end = inputs[20];
        this.longitude_end = inputs[21];
        this.dpcapacity_end = inputs[22];
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getStoptime() {
        return stoptime;
    }

    public void setStoptime(String stoptime) {
        this.stoptime = stoptime;
    }

    public String getTripduration() {
        return tripduration;
    }

    public void setTripduration(String tripduration) {
        this.tripduration = tripduration;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public String getFrom_station_id() {
        return from_station_id;
    }

    public void setFrom_station_id(String from_station_id) {
        this.from_station_id = from_station_id;
    }

    public String getFrom_station_name() {
        return from_station_name;
    }

    public void setFrom_station_name(String from_station_name) {
        this.from_station_name = from_station_name;
    }

    public String getLatitude_start() {
        return latitude_start;
    }

    public void setLatitude_start(String latitude_start) {
        this.latitude_start = latitude_start;
    }

    public String getLongitude_start() {
        return longitude_start;
    }

    public void setLongitude_start(String longitude_start) {
        this.longitude_start = longitude_start;
    }

    public String getDpcapacity_start() {
        return dpcapacity_start;
    }

    public void setDpcapacity_start(String dpcapacity_start) {
        this.dpcapacity_start = dpcapacity_start;
    }

    public String getTo_station_id() {
        return to_station_id;
    }

    public void setTo_station_id(String to_station_id) {
        this.to_station_id = to_station_id;
    }

    public String getTo_station_name() {
        return to_station_name;
    }

    public void setTo_station_name(String to_station_name) {
        this.to_station_name = to_station_name;
    }

    public String getLatitude_end() {
        return latitude_end;
    }

    public void setLatitude_end(String latitude_end) {
        this.latitude_end = latitude_end;
    }

    public String getLongitude_end() {
        return longitude_end;
    }

    public void setLongitude_end(String longitude_end) {
        this.longitude_end = longitude_end;
    }

    public String getDpcapacity_end() {
        return dpcapacity_end;
    }

    public void setDpcapacity_end(String dpcapacity_end) {
        this.dpcapacity_end = dpcapacity_end;
    }

    @Override
    public String toString() {
        return "Document{" +
                "trip_id='" + trip_id + '\'' +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", week='" + week + '\'' +
                ", day='" + day + '\'' +
                ", hour='" + hour + '\'' +
                ", usertype='" + usertype + '\'' +
                ", gender='" + gender + '\'' +
                ", starttime='" + starttime + '\'' +
                ", stoptime='" + stoptime + '\'' +
                ", tripduration='" + tripduration + '\'' +
                ", temperature='" + temperature + '\'' +
                ", events='" + events + '\'' +
                ", from_station_id='" + from_station_id + '\'' +
                ", from_station_name='" + from_station_name + '\'' +
                ", latitude_start='" + latitude_start + '\'' +
                ", longitude_start='" + longitude_start + '\'' +
                ", dpcapacity_start='" + dpcapacity_start + '\'' +
                ", to_station_id='" + to_station_id + '\'' +
                ", to_station_name='" + to_station_name + '\'' +
                ", latitude_end='" + latitude_end + '\'' +
                ", longitude_end='" + longitude_end + '\'' +
                ", dpcapacity_end='" + dpcapacity_end + '\'' +
                '}';
    }
}
