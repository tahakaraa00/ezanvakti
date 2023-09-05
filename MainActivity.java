package com.hadi.ezanvakti;


import static android.location.LocationManager.GPS_PROVIDER;
import static java.lang.Math.abs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements SensorEventListener {

    int MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS;
    double currentDegree2 = 0f;
    // define the display assembly compass picture
    private ImageView image;
    public double GPSEnlem, GPSBoylam;

    public NamazVaktiHesaplama hes;

    // record the compass picture angle turned
    private float currentDegree = 0f;


    private double imsakVakti = -1;
    private double imsakVaktiAcisi = -1;
    private double transitTime = -1;
    private double ogleVakti = -1;
    private double ogleVaktiAcisi = -1;
    private double ikindiVakti = -1;
    private double ikindiVaktiAcisi = -1;
    private double yatsiVakti = -1;
    private double yatsiVaktiAcisi = -1;
    private double kibleVakti = -1;
    private double kibleAcisi = -1;
    private double gunUzunlugu = -1;
    private double geceUzunlugu = -1;
    private double sunAzimuth = -1;
    private double sunElevation = -1;
    private double decSun;
    private double civilGunDogumu = -1;
    private double gunesVakti = -1;
    private double gunesVaktiAcisi = -1;
    private double civilGunBatimi = -1;
    private double aksamVakti = -1;
    private double aksamVaktiAcisi = -1;
    private double gunesDogumAcisi = -1;
    private double gunesBatimAcisi = -1;
    private double simdikiSaat = -1;
    private double moonAzimuth = -1;
    private double moonElevation = -1;
    private double moonAge = -1;
    private double moonFracture = -1;
    private double moonRotationAngle = -1;
    private double kerVaktiAcisiGunes = -1;
    private double kerVaktiAcisiOgle = -1;
    private double kerVaktiAcisiAksam = -1;
    private boolean keratVaktiStatus;

    private static double KERAT_VAKTI_MIKTARI = 45.0D;

    private double yuvarla60(double tmp)
    {
        return Math.round(tmp * 60.0) / 60.0;
    }


    private double yuvarla10(double tmp)
    {
        return Math.round(tmp * 10.0) / 10.0;
    }

    public double Saat()
    {
        return simdikiSaat;
    }

    public double Imsak_Vakti()
    {
        return yuvarla60(imsakVakti);
    }

    public double Imsak_Vakti_Acisi()
    {
        return yuvarla60(imsakVaktiAcisi);
    }

    public double Gunes_Vakti()
    {
        return yuvarla60(gunesVakti);
    }

    public double Gunes_Vakti_Acisi()
    {
        return yuvarla60(gunesVaktiAcisi);
    }

    public double Ogle_Vakti()
    {
        return yuvarla60(ogleVakti);
    }

    public double Ogle_Vakti_Acisi()
    {
        return yuvarla60(ogleVaktiAcisi);
    }

    public double Ikindi_Vakti()
    {
        return yuvarla60(ikindiVakti);
    }

    public double Ikindi_Vakti_Acisi()
    {
        return yuvarla60(ikindiVaktiAcisi);
    }

    public double Aksam_Vakti()
    {
        return yuvarla60(aksamVakti);
    }

    public double Aksam_Vakti_Acisi()
    {
        return yuvarla60(aksamVaktiAcisi);
    }

    public double Yatsi_Vakti()
    {
        return yuvarla60(yatsiVakti);
    }

    public double Yatsi_Vakti_Acisi()
    {
        return yuvarla60(yatsiVaktiAcisi);
    }

    public double Ker_Vakti_Acisi_Gunes()
    {
        return yuvarla60(kerVaktiAcisiGunes);
    }

    public double Ker_Vakti_Acisi_Ogle()
    {
        return yuvarla60(kerVaktiAcisiOgle);
    }

    public double Ker_Vakti_Acisi_Aksam()
    {
        return yuvarla60(kerVaktiAcisiAksam);
    }

    public boolean Ker_Vakti_Durumu() { return keratVaktiStatus;}

    public double Kible_Vakti()
    {
        return yuvarla60(kibleVakti);
    }

    public double Kible_Acisi()
    {
        return yuvarla10(kibleAcisi);
    }

    public double Gun_Uzunlugu()
    {
        return yuvarla60(gunUzunlugu);
    }

    public double Gece_Uzunlugu()
    {
        return yuvarla60(geceUzunlugu);
    }

    public double Gunes_Azimuth()
    {
        return yuvarla10(sunAzimuth);
    }

    public double Gunes_Elevation()
    {
        return yuvarla10(sunElevation);
    }

    public double Moon_Azimuth()
    {
        return yuvarla10(moonAzimuth);
    }

    public double Moon_Elevation()
    {
        return yuvarla10(moonElevation);
    }

    public double Moon_Age() {return moonAge;}

    public double Moon_Fracture() { return moonFracture;}

    public double Moon_Rotation() { return moonRotationAngle;}

    public double Civil_Gun_Dogumu()
    {
        return civilGunDogumu;
    }

    public double Civil_Gun_Batimi()
    {
        return civilGunBatimi;
    }

    public double GGunes_Dogum_Acisi()
    {
        return yuvarla10(gunesDogumAcisi);
    }

    public double GGunes_Batim_Acisi()
    {
        return yuvarla10(gunesBatimAcisi);
    }

    public String ConvertTime(double a) {
        String s;
        int hour;
        int minute;
        String strHour;
        String strMinute;

        hour = (int) Math.floor(a);
        minute = (int) Math.round(((a - Math.floor(a)) * 60));

        if (minute == 60) {
            hour++;
            minute = 0;
        }

        strHour = String.valueOf(hour);
        strMinute = String.valueOf(minute);

        if (strHour.length() < 2) strHour = "0" + strHour;
        if (strMinute.length() < 2) strMinute = "0" + strMinute;
        s = strHour + ":" + strMinute;

        return s;
    }

    // device sensor manager
    private SensorManager mSensorManager;
    Button mButton;
    Button btnSaatSec;
    TextView etSaat;
    TextView etSaat2;
    TextView etSaat3;

    TextView etSaat4;

    TextView etSaat5;

    TextView etSaat6;
    Context context = this;
    EditText myazikismi1;
    EditText myazikismi2;

    TextView mText;
    TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        checkLocationPermission();
        checkNotificationPermission();

        final String gpsAcildi = "GPS Acıldı";
        final String gpsKapatildi = "GPS Kapalı";
        // our compass image
        image = (ImageView) findViewById(R.id.Compass);
        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        final TextView konumText = (TextView) findViewById(R.id.textView3);
        etSaat = (TextView) findViewById(R.id.edittext_saat);
        etSaat2 = (TextView) findViewById(R.id.edittext_saat2);
        etSaat3 = (TextView) findViewById(R.id.edittext_saat3);
        etSaat4 = (TextView) findViewById(R.id.edittext_saat4);
        etSaat5 = (TextView) findViewById(R.id.edittext_saat5);
        etSaat6 = (TextView) findViewById(R.id.edittext_saat6);
        String istikamet = getResources().getString(R.string.istikamet);
        String bitti = getResources().getString(R.string.bitti);

        LocationManager konumYoneticisi = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener konumDinleyicisi = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getApplicationContext(), gpsAcildi, Toast.LENGTH_SHORT).show();
                konumText.setText("GPS Veri bilgileri Alınıyor...");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getApplicationContext(), gpsKapatildi, Toast.LENGTH_SHORT).show();
                konumText.setText("GPS Bağlantı Bekleniyor...");
            }

            @Override
            public void onLocationChanged(Location loc) {
                GPSEnlem = loc.getLatitude();
                GPSBoylam = loc.getLongitude();

                String Text = "Bulunduğunuz konum bilgileri : \n" + "Latitud = " + loc.getLatitude() + "\nLongitud = " + loc.getLongitude();
                konumText.setText(Text);


            }

        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);

        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            return;
        }


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            return;
        }
        konumYoneticisi.requestLocationUpdates(GPS_PROVIDER, 10000, 20, konumDinleyicisi);
        konumYoneticisi.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 20, konumDinleyicisi);

        hes = new NamazVaktiHesaplama();

        hes.hesapla(this, 3.0, false, false, 39.76, 30.54, 0);

        //public void hesapla(Context context, double gmtSaatFarki, boolean yazSaatiUygulamasi, boolean ertesiGun, double enlem, double boylam, int plusDays)


    }



    private double BoundConvert(double a, double b)
    {
        if (a < 0) return (a + b);
        else if (a >= b) return (a - b * Math.floor(a / b));
        return a;
    }

    private double T(double angle, double L, double D)
    {
        double temp = Math.toDegrees(Math.acos((-1.0D * Math.sin(Math.toRadians(angle)) - Math.sin(Math.toRadians(L)) *
                Math.sin(Math.toRadians(D))) / (Math.cos(Math.toRadians(L)) * Math.cos(Math.toRadians(D)))));
        temp = BoundConvert(temp, 360.0D);
        temp = temp / 15.0D;
        return temp;
    }

    private double A(double k, double L, double D)
    {
        double temp = Math.toDegrees(Math.acos((Math.sin(acot(k + Math.tan(Math.toRadians(L - D)))) - Math.sin(k * Math.toRadians(L)) *
                Math.sin(Math.toRadians(D))) / (Math.cos(Math.toRadians(L)) * Math.cos(Math.toRadians(D)))));
        temp = BoundConvert(temp, 360.0D);
        temp = temp / 15.0D;
        return temp;
    }

    private double acot(double a)
    {
        return (Math.atan(1.0D / a));
    }















    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double x1=GPSBoylam;
        double y1=GPSEnlem;
        double x2=39.82623;
        double y2=21.42256;
        double ylambda = abs(y2-y1);
        double xlambda = abs(x2-x1);
        double thetam=Math.atan(ylambda/xlambda);
        if(y2>y1){
            double Azimut= (thetam * (180/3.141592653589793238462643383));
        }

        double Azimut = (thetam * (180 / 3.141592653589793238462643383)) + 90;


        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree)+ " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                (float)(currentDegree2),
                (float) (-degree +Azimut ),
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);


        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree2 = (float) (-degree+Azimut );


         double enlem=GPSEnlem;
         double boylam=GPSBoylam;
         boolean yazSaatiUygulamasi=false;


        int plusDays=0;

        double gmtSaatFarki=3.0;


        boolean ertesiGun = false;
        if (yazSaatiUygulamasi) gmtSaatFarki = gmtSaatFarki + 1;
        if (yazSaatiUygulamasi) gmtSaatFarki = gmtSaatFarki + 1;

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.SECOND, (int) (-3600.0 * gmtSaatFarki));
        if (ertesiGun) cal.add(Calendar.DATE, 1);
        cal.add(Calendar.DATE, plusDays);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        SunMoonCalculator smCalculatorCurrent, smCalculatorStartOfDay;

        try
        {
            //Hesaplamayı current ve startofday olarak ikiye ayırıyoruz. Eğer current kullanırsak belli bir saatten sonra ertesi güne göre hesaplama yapılıyor
            smCalculatorCurrent = new SunMoonCalculator(year, month, day, hour, minute, second, boylam * SunMoonCalculator.DEG_TO_RAD, enlem * SunMoonCalculator.DEG_TO_RAD);
            smCalculatorStartOfDay = new SunMoonCalculator(year, month, day, 0, 0, 0, boylam * SunMoonCalculator.DEG_TO_RAD, enlem * SunMoonCalculator.DEG_TO_RAD);
            smCalculatorCurrent.calcSunAndMoon();
            smCalculatorStartOfDay.calcSunAndMoon();
            smCalculatorCurrent.setTwilight(SunMoonCalculator.TWILIGHT.TWILIGHT_CIVIL);
            smCalculatorStartOfDay.setTwilight(SunMoonCalculator.TWILIGHT.TWILIGHT_CIVIL);
        }
        catch (Exception e)
        {
            return;
        }

        this.moonAzimuth = smCalculatorCurrent.moon.azimuth * SunMoonCalculator.RAD_TO_DEG;
        this.moonElevation = smCalculatorCurrent.moon.elevation * SunMoonCalculator.RAD_TO_DEG;
        this.moonAge = smCalculatorCurrent.moonAge;
        this.moonFracture = smCalculatorCurrent.moon.illuminationPhase;
        this.moonRotationAngle = smCalculatorCurrent.getMoonDiskOrientationAngles()[4];

        this.decSun = smCalculatorStartOfDay.sun.declination * smCalculatorStartOfDay.RAD_TO_DEG;
        this.sunElevation = smCalculatorCurrent.sun.elevation * SunMoonCalculator.RAD_TO_DEG;
        this.sunAzimuth = smCalculatorCurrent.sun.azimuth * SunMoonCalculator.RAD_TO_DEG;

        try
        {
            civilGunDogumu = gmtSaatFarki + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.rise)[3] + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.rise)[4] / 60.0d + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.rise)[4] / 3600.0d;
            civilGunBatimi = gmtSaatFarki + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.set)[3] + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.set)[4] / 60.0d + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.set)[4] / 3600.0d;
            transitTime = gmtSaatFarki + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.transit)[3] + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.transit)[4] / 60.0d + SunMoonCalculator.getDate(smCalculatorStartOfDay.sun.transit)[4] / 3600.0d;
        }
        catch (Exception ex)
        {
        }

        simdikiSaat = cal.get(Calendar.HOUR_OF_DAY) / 1.0D + cal.get(Calendar.MINUTE) / 60.0D;

        double imsakKaranlikAcisi;
        double yatsiKaranlikAcisi;

        imsakKaranlikAcisi = 18.0D;
        yatsiKaranlikAcisi = 17.0D;

        kibleVakti = 0.0D;
        gunesVakti = transitTime - T(1.0D, enlem, decSun);
        aksamVakti = transitTime + T(1.0D, enlem, decSun);
        imsakVakti = transitTime - T(imsakKaranlikAcisi, enlem, decSun);
        ikindiVakti = transitTime + A(1, enlem, decSun);
        yatsiVakti = transitTime + T(yatsiKaranlikAcisi, enlem, decSun);

        kibleAcisi = Math.atan2(Math.sin(Math.toRadians(39.82475D - boylam)), (Math.cos(Math.toRadians(enlem)) * Math
                .tan(Math.toRadians(21.42111D)) - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(39.82475D - boylam))));

        if (boylam < 39.82475D) kibleAcisi = 180D * kibleAcisi / Math.PI;
        else kibleAcisi = 360D + 180D * kibleAcisi / Math.PI;

        double min_delta = 10000.0D;

        double v = Math.cos(Math.toRadians(enlem)) * Math.tan(Math.toRadians(decSun));
        double z;

        for (double i = gunesVakti; i < aksamVakti; i = i + 1.0 / 60.0)
        {
            z = Math.atan(Math.sin(Math.toRadians(15.0D * (i - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (i - transitTime)))));
            z = 180.0D - Math.toDegrees(z);

            if (Math.abs(z - kibleAcisi) < min_delta)
            {
                min_delta = Math.abs(z - kibleAcisi);
                kibleVakti = i;
            }
        }
        kibleAcisi = (double) Math.round(kibleAcisi * 10.0D) / 10.0D;

        if (enlem > 62) enlem = 62;

        if (enlem >= 45)
        {
            double Aksam_Imsak = (imsakVakti - aksamVakti + 24.0D) / 3.0D;
            if (Aksam_Imsak > 1.3333D) yatsiVakti = aksamVakti + 1.3333D;
            else yatsiVakti = aksamVakti + Aksam_Imsak;
        }

        if (enlem < 45)
        {
            gunesVakti= gunesVakti - 6.0D / 60.0D;
            ogleVakti = transitTime + 5.0d / 60.0d;
            ikindiVakti = ikindiVakti + 5.0D / 60.0D;
            aksamVakti = aksamVakti + 7.0D / 60.0D;
            yatsiVakti = yatsiVakti + 2.0D / 60.0D;
            imsakVakti = imsakVakti - 2.0D / 60.0D;
        }
        else if (enlem >= 45)
        {

            gunesVakti = gunesVakti - 5.0D / 60.0D;
            ogleVakti = transitTime + 2.0d / 60.0d;
            ikindiVakti = ikindiVakti + 5.0D / 60.0D;
            aksamVakti = aksamVakti + 5.0D / 60.0D;
            yatsiVakti = yatsiVakti + 2.0D / 60.0D;
            imsakVakti = imsakVakti - 5.0D / 60.0D;
        }

        SharedPreferences mPrefs;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        double ImsakKalibrasyon = 0.0D;
        double GunesKalibrasyon = 0.0D;
        double OgleKalibrasyon = 0.0D;
        double IkindiKalibrasyon = 0.0D;
        double AksamKalibrasyon = 0.0D;
        double YatsiKalibrasyon = 0.0D;

        try
        {
            ImsakKalibrasyon = Double.parseDouble(mPrefs.getString("prfImsakKalibrasyon", "0")) / 60.0D;
        }
        catch (Exception ex)
        {

        }

        try
        {
            GunesKalibrasyon = Double.parseDouble(mPrefs.getString("prfGunesKalibrasyon", "0")) / 60.0D;
        }
        catch (Exception ex)
        {

        }

        try
        {
            OgleKalibrasyon = Double.parseDouble(mPrefs.getString("prfOgleKalibrasyon", "0")) / 60.0D;
        }
        catch (Exception ex)
        {

        }

        try
        {
            IkindiKalibrasyon = Double.parseDouble(mPrefs.getString("prfIkindiKalibrasyon", "0")) / 60.0D;
        }
        catch (Exception ex)
        {

        }

        try
        {
            AksamKalibrasyon = Double.parseDouble(mPrefs.getString("prfAksamKalibrasyon", "0")) / 60.0D;
        }
        catch (Exception ex)
        {

        }

        try
        {
            YatsiKalibrasyon = Double.parseDouble(mPrefs.getString("prfYatsiKalibrasyon", "0")) / 60.0D;
        }
        catch (Exception ex)
        {

        }

        gunesVakti = gunesVakti + GunesKalibrasyon;
        ogleVakti = ogleVakti + OgleKalibrasyon;
        ikindiVakti = ikindiVakti + IkindiKalibrasyon;
        aksamVakti = aksamVakti + AksamKalibrasyon;
        yatsiVakti = yatsiVakti + YatsiKalibrasyon;
        imsakVakti = imsakVakti + ImsakKalibrasyon;
        String imsakVaktiStr = ConvertTime(imsakVakti);
        etSaat.setText("İmsak    "+imsakVaktiStr);


        String gunesVaktiStr = ConvertTime(gunesVakti);
        etSaat2.setText("Güneş    "+gunesVaktiStr);


        String ogleVaktiStr =ConvertTime(ogleVakti);
        etSaat3.setText("Öğle    "+ogleVaktiStr);


        String ikindiVaktiStr =ConvertTime(ikindiVakti);
        etSaat4.setText("İkindi    "+ikindiVaktiStr);


        String aksamVaktiStr =ConvertTime(aksamVakti);
        etSaat5.setText("Aksam    "+aksamVaktiStr);


        String yatsiVaktiStr =ConvertTime(yatsiVakti);
        etSaat6.setText("Yatsı    "+yatsiVaktiStr);

        cal=Calendar.getInstance();
        keratVaktiStatus=false;
        if (cal.get(Calendar.HOUR_OF_DAY)+ cal.get(Calendar.MINUTE)/60.0D>gunesVakti && cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0D < gunesVakti + KERAT_VAKTI_MIKTARI / 60.0D) keratVaktiStatus=true;
        if (cal.get(Calendar.HOUR_OF_DAY)+ cal.get(Calendar.MINUTE)/60.0D>ogleVakti- KERAT_VAKTI_MIKTARI /60.0D && cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0D < ogleVakti) keratVaktiStatus=true;
        if (cal.get(Calendar.HOUR_OF_DAY)+ cal.get(Calendar.MINUTE)/60.0D>aksamVakti-KERAT_VAKTI_MIKTARI/60.0D && cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0D < aksamVakti) keratVaktiStatus=true;


        gunesDogumAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (civilGunDogumu - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (civilGunDogumu - transitTime)))));
        gunesDogumAcisi = 180.0D - Math.toDegrees(gunesDogumAcisi);

        gunesBatimAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (civilGunBatimi - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (civilGunBatimi - transitTime)))));
        gunesBatimAcisi = 180.0D - Math.toDegrees(gunesBatimAcisi);

        gunesVaktiAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (gunesVakti - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (gunesVakti - transitTime)))));
        gunesVaktiAcisi = 180.0D - Math.toDegrees(gunesVaktiAcisi);

        ogleVaktiAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (ogleVakti - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (ogleVakti - transitTime)))));
        ogleVaktiAcisi = 180.0D - Math.toDegrees(ogleVaktiAcisi);

        ikindiVaktiAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (ikindiVakti - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (ikindiVakti - transitTime)))));
        ikindiVaktiAcisi = 180.0D - Math.toDegrees(ikindiVaktiAcisi);

        aksamVaktiAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (aksamVakti - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (aksamVakti - transitTime)))));
        aksamVaktiAcisi = 180.0D - Math.toDegrees(aksamVaktiAcisi);

        yatsiVaktiAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (yatsiVakti - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (yatsiVakti - transitTime)))));
        yatsiVaktiAcisi = 180.0D - Math.toDegrees(yatsiVaktiAcisi);

        imsakVaktiAcisi = Math.atan(Math.sin(Math.toRadians(15.0D * (imsakVakti - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (imsakVakti - transitTime)))));
        imsakVaktiAcisi = 180.0D - Math.toDegrees(imsakVaktiAcisi);

        kerVaktiAcisiGunes = Math.atan(Math.sin(Math.toRadians(15.0D * (gunesVakti + KERAT_VAKTI_MIKTARI / 60.0D - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (gunesVakti + KERAT_VAKTI_MIKTARI / 60.0D - transitTime)))));
        kerVaktiAcisiGunes = 180.0D - Math.toDegrees(kerVaktiAcisiGunes);

        kerVaktiAcisiOgle = Math.atan(Math.sin(Math.toRadians(15.0D * (ogleVakti - KERAT_VAKTI_MIKTARI / 60.0D - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (transitTime - KERAT_VAKTI_MIKTARI / 60.0D - transitTime)))));
        kerVaktiAcisiOgle = 180.0D - Math.toDegrees(kerVaktiAcisiOgle);

        kerVaktiAcisiAksam = Math.atan(Math.sin(Math.toRadians(15.0D * (aksamVakti - KERAT_VAKTI_MIKTARI / 60.0D - transitTime))) / (v - Math.sin(Math.toRadians(enlem)) * Math.cos(Math.toRadians(15.0D * (aksamVakti - KERAT_VAKTI_MIKTARI / 60.0D - transitTime)))));
        kerVaktiAcisiAksam = 180.0D - Math.toDegrees(kerVaktiAcisiAksam);

        if (gunesBatimAcisi < 180.0D) gunesBatimAcisi = gunesBatimAcisi + 180.0D;
        if (ikindiVaktiAcisi < 180.0D) ikindiVaktiAcisi = ikindiVaktiAcisi + 180.0D;
        if (aksamVaktiAcisi < 180.0D) aksamVaktiAcisi = aksamVaktiAcisi + 180.0D;
        if (yatsiVaktiAcisi < 180.0D) yatsiVaktiAcisi = yatsiVaktiAcisi + 180.0D;
        if (gunesDogumAcisi >= 180.0D && gunesDogumAcisi <= 270.0D) gunesDogumAcisi = gunesDogumAcisi - 180.0D;
        if (gunesVaktiAcisi >= 180.0D && gunesVaktiAcisi <= 270.0D) gunesVaktiAcisi = gunesVaktiAcisi - 180.0D;
        if (imsakVaktiAcisi >= 180.0D && imsakVaktiAcisi <= 270.0D) imsakVaktiAcisi = imsakVaktiAcisi - 180.0D;
        if (kerVaktiAcisiGunes >= 180.0D && kerVaktiAcisiGunes <= 270.0D) kerVaktiAcisiGunes = kerVaktiAcisiGunes - 180.0D;
        if (kerVaktiAcisiAksam < 180.0D) kerVaktiAcisiAksam = kerVaktiAcisiAksam + 180.0D;
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    public static final int MY_PERMISSIONS_REQUEST_POST_NOTIFICATION = 1;

    public boolean checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("R.string.title_notification_permission")
                        .setMessage(R.string.text_notification_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                        MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
            }
            return false;
        } else {
            return true;
        }
    }
    public void queryWithParamsAndProxy()throws Exception
    {
        java.util.Properties systemProperties = System.getProperties();
        systemProperties.setProperty("http.proxyHost","your-proxy.url.com");
        systemProperties.setProperty("http.proxyPort","1234");

        String path ="https://ezanvakti.herokuapp.com/?param1="+
                java.net.URLEncoder.encode("value1","UTF-8")+
                "&param2="+java.net.URLEncoder.encode("value2","UTF-8");

        java.net.URL url = new java.net.URL(path);
        /* open the connection */
        java.net.HttpURLConnection con = (java.net.HttpURLConnection)url.openConnection();
        /* get the incoming data */
        if (con.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
            throw new RuntimeException(
                    "Error calling Search API (HTTP status " + con.getResponseCode() + ")");
        }
        /* get the incoming data */
        java.io.InputStream stream = con.getInputStream();

    }

}