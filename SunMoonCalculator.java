package com.hadi.ezanvakti;

public class SunMoonCalculator {

    /** Radians to degrees. */
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    /** Degrees to radians. */
    public static final double DEG_TO_RAD = 1.0 / RAD_TO_DEG;

    /** Astronomical Unit in km. As defined by JPL. */
    public static final double AU = 149597870.691;

    /** Earth equatorial radius in km. IERS 2003 Conventions. */
    public static final double EARTH_RADIUS = 6378.1366;

    /** Two times Pi. */
    public static final double TWO_PI = 2.0 * Math.PI;

    /** Pi divided by two. */
    public static final double PI_OVER_TWO = Math.PI / 2.0;

    /** Julian century conversion constant = 100 * days per year. */
    public static final double JULIAN_DAYS_PER_CENTURY = 36525.0;

    /** Seconds in one day. */
    public static final double SECONDS_PER_DAY = 86400;

    /** Our default epoch. The Julian Day which represents noon on 2000-01-01. */
    public static final double J2000 = 2451545.0;

    /** The set of twilights to calculate (types of rise/set events). */
    public static enum TWILIGHT {
        /**
         * Event ID for calculation of rising and setting times for astronomical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -18 degrees of geometrical elevation below the
         * astronomical horizon. At this time astronomical observations are possible
         * because the sky is dark enough.
         */
        TWILIGHT_ASTRONOMICAL,
        /**
         * Event ID for calculation of rising and setting times for nautical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -12 degrees of geometric elevation below the
         * astronomical horizon.
         */
        TWILIGHT_NAUTICAL,
        /**
         * Event ID for calculation of rising and setting times for civil twilight.
         * In this case, the calculated time will be the time when the center of the
         * object is at -6 degrees of geometric elevation below the astronomical
         * horizon.
         */
        TWILIGHT_CIVIL,
        /**
         * The standard value of 34' for the refraction at the local horizon.
         */
        HORIZON_34arcmin
    };

    /** The set of events to calculate (rise/set/transit events). */
    public static enum EVENT {
        /** Rise. */
        RISE,
        /** Set. */
        SET,
        /** Transit. */
        TRANSIT
    }

    /** The set of phases to compute the moon phases. */
    public static enum MOONPHASE {
        /** New Moon phase. */
        NEW_MOON ("New Moon:        ", 0),
        /** Crescent quarter phase. */
        CRESCENT_QUARTER ("Crescent quarter:", 0.25),
        /** Full Moon phase. */
        FULL_MOON ("Full Moon:       ", 0.5),
        /** Descent quarter phase. */
        DESCENT_QUARTER ("Descent quarter: ", 0.75);

        /** Phase name. */
        public String phaseName;
        /** Phase value. */
        public double phase;

        private MOONPHASE(String name, double ph) {
            phaseName = name;
            phase = ph;
        }
    }

    /** Input values. */
    private double jd_UT = 0, t = 0, obsLon = 0, obsLat = 0, TTminusUT = 0;
    private TWILIGHT twilight = TWILIGHT.HORIZON_34arcmin;

    /**
     * Class to hold the results of ephemerides.
     * @author T. Alonso Albi - OAN (Spain)
     */
    public class Ephemeris {
        private Ephemeris(double azi, double alt, double rise2, double set2,
                          double transit2, double transit_alt, double ra, double dec,
                          double dist, double eclLon, double eclLat, double angR) {
            azimuth = azi;
            elevation = alt;
            rise = rise2;
            set = set2;
            transit = transit2;
            transitElevation = transit_alt;
            rightAscension = ra;
            declination = dec;
            distance = dist;
            illuminationPhase = 100;
            eclipticLongitude = eclLon;
            eclipticLatitude = eclLat;
            angularRadius = angR;
        }

        /** Values for azimuth, elevation, rise, set, and transit for the Sun. Angles in radians, rise ...
         * as Julian days in UT. Distance in AU. */
        public double azimuth, elevation, rise, set, transit, transitElevation, distance, rightAscension,
                declination, illuminationPhase, eclipticLongitude, eclipticLatitude, angularRadius;
    }

    /** Ephemeris for the Sun and Moon bodies. */
    public Ephemeris sun, moon;

    /** Moon's age in days as an independent variable. */
    public double moonAge;


    /**
     * Main constructor for Sun/Moon calculations. Time should be given in
     * Universal Time (UT), observer angles in radians.
     * @param year The year.
     * @param month The month.
     * @param day The day.
     * @param h The hour.
     * @param m Minute.
     * @param s Second.
     * @param obsLon Longitude for the observer.
     * @param obsLat Latitude for the observer.
     * @throws Exception If the date does not exists.
     */
    public SunMoonCalculator(int year, int month, int day, int h, int m, int s,
                             double obsLon, double obsLat) throws Exception {
        double jd = toJulianDay(year, month, day, h, m, s);

        TTminusUT = 0;
        if (year > -600 && year < 2200) {
            double x = year + (month - 1 + day / 30.0) / 12.0;
            double x2 = x * x, x3 = x2 * x, x4 = x3 * x;
            if (year < 1600) {
                TTminusUT = 10535.328003326353 - 9.995238627481024 * x + 0.003067307630020489 * x2 - 7.76340698361363E-6 * x3 + 3.1331045394223196E-9 * x4 +
                        8.225530854405553E-12 * x2 * x3 - 7.486164715632051E-15 * x4 * x2 + 1.9362461549678834E-18 * x4 * x3 - 8.489224937827653E-23 * x4 * x4;
            } else {
                TTminusUT = -1027175.3477559977 + 2523.256625418965 * x - 1.885686849058459 * x2 + 5.869246227888417E-5 * x3 + 3.3379295816475025E-7 * x4 +
                        1.7758961671447929E-10 * x2 * x3 - 2.7889902806153024E-13 * x2 * x4 + 1.0224295822336825E-16 * x3 * x4 - 1.2528102370680435E-20 * x4 * x4;
            }
        }
        this.obsLon = obsLon;
        this.obsLat = obsLat;
        setUTDate(jd);
    }

    private double toJulianDay(int year, int month, int day, int h, int m, int s) throws Exception {
        // The conversion formulas are from Meeus, chapter 7.
        boolean julian = false; // Use Gregorian calendar
        if (year < 1582 || (year == 1582 && month <= 10) || (year == 1582 && month == 10 && day < 15)) julian = true;
        int D = day;
        int M = month;
        int Y = year;
        if (M < 3) {
            Y--;
            M += 12;
        }
        int A = Y / 100;
        int B = julian ? 0 : 2 - A + A / 4;

        double dayFraction = (h + (m + (s / 60.0)) / 60.0) / 24.0;
        double jd = dayFraction + (int) (365.25D * (Y + 4716)) + (int) (30.6001 * (M + 1)) + D + B - 1524.5;

        if (jd < 2299160.0 && jd >= 2299150.0)
            throw new Exception("invalid julian day " + jd + ". This date does not exist.");

        return jd;
    }

    /**
     * Sets the rise/set times to return. Default is for the local horizon.
     * @param t The Twilight.
     */
    public void setTwilight(TWILIGHT t) {
        this.twilight = t;
    }

    private void setUTDate(double jd) {
        this.jd_UT = jd;
        this.t = (jd + TTminusUT / SECONDS_PER_DAY  - J2000) / JULIAN_DAYS_PER_CENTURY;
    }

    /** Calculates everything for the Sun and the Moon. */
    public void calcSunAndMoon() {
        double jd = this.jd_UT;

        // First the Sun
        sun = doCalc(getSun(), false);

        int niter = 3; // Number of iterations to get accurate rise/set/transit times
        sun.rise = obtainAccurateRiseSetTransit(sun.rise, EVENT.RISE, niter, true);
        sun.set = obtainAccurateRiseSetTransit(sun.set, EVENT.SET, niter, true);
        sun.transit = obtainAccurateRiseSetTransit(sun.transit, EVENT.TRANSIT, niter, true);
        if (sun.transit == -1) {
            sun.transitElevation = 0;
        } else {
            // Update Sun's maximum elevation
            setUTDate(sun.transit);
            sun.transitElevation = doCalc(getSun(), false).transitElevation;
        }

        // Now Moon
        setUTDate(jd);
        moon = doCalc(getMoon(), false);
        double ma = moonAge;

        niter = 5; // Number of iterations to get accurate rise/set/transit times
        moon.rise = obtainAccurateRiseSetTransit(moon.rise, EVENT.RISE, niter, false);
        moon.set = obtainAccurateRiseSetTransit(moon.set, EVENT.SET, niter, false);
        moon.transit = obtainAccurateRiseSetTransit(moon.transit, EVENT.TRANSIT, niter, false);
        if (moon.transit == -1) {
            moon.transitElevation = 0;
        } else {
            // Update Moon's maximum elevation
            setUTDate(moon.transit);
            getSun();
            moon.transitElevation = doCalc(getMoon(), false).transitElevation;
        }
        setUTDate(jd);
        moonAge = ma;

        // Compute illumination phase percentage for the Moon (do not use for other bodies!)
        double dlon = moon.rightAscension - sun.rightAscension;
        double elong = Math.acos(Math.sin(sun.declination) * Math.sin(moon.declination) +
                Math.cos(sun.declination) * Math.cos(moon.declination) * Math.cos(dlon));
        moon.illuminationPhase = 100 * (1.0 - Math.cos(elong)) * 0.5;
    }

    // Formulae here is a simplification of the expansion from
    // "Planetary Programs and Tables" by Pierre Bretagnon and
    // Jean-Louis Simon, Willman-Bell, 1986. This source also
    // have expansions for ephemerides of planets
    private static double sun_elements[][] = {
            new double[] { 403406.0, 0.0, 4.721964, 1.621043 },
            new double[] { 195207.0, -97597.0, 5.937458, 62830.348067 },
            new double[] { 119433.0, -59715.0, 1.115589, 62830.821524 },
            new double[] { 112392.0, -56188.0, 5.781616, 62829.634302 },
            new double[] { 3891.0, -1556.0, 5.5474, 125660.5691 },
            new double[] { 2819.0, -1126.0, 1.512, 125660.9845 },
            new double[] { 1721.0, -861.0, 4.1897, 62832.4766 },
            new double[] { 0.0, 941.0, 1.163, .813 },
            new double[] { 660.0, -264.0, 5.415, 125659.31 },
            new double[] { 350.0, -163.0, 4.315, 57533.85 },
            new double[] { 334.0, 0.0, 4.553, -33.931 },
            new double[] { 314.0, 309.0, 5.198, 777137.715 },
            new double[] { 268.0, -158.0, 5.989, 78604.191 },
            new double[] { 242.0, 0.0, 2.911, 5.412 },
            new double[] { 234.0, -54.0, 1.423, 39302.098 },
            new double[] { 158.0, 0.0, .061, -34.861 },
            new double[] { 132.0, -93.0, 2.317, 115067.698 },
            new double[] { 129.0, -20.0, 3.193, 15774.337 },
            new double[] { 114.0, 0.0, 2.828, 5296.67 },
            new double[] { 99.0, -47.0, .52, 58849.27 },
            new double[] { 93.0, 0.0, 4.65, 5296.11 },
            new double[] { 86.0, 0.0, 4.35, -3980.7 },
            new double[] { 78.0, -33.0, 2.75, 52237.69 },
            new double[] { 72.0, -32.0, 4.5, 55076.47 },
            new double[] { 68.0, 0.0, 3.23, 261.08 },
            new double[] { 64.0, -10.0, 1.22, 15773.85 }
    };

    private double[] getSun() {
        double L = 0.0, R = 0.0;
        double t2 = t * 0.01;
        for (int i = 0; i < sun_elements.length; i++) {
            double v = sun_elements[i][2] + sun_elements[i][3] * t2;
            double u = normalizeRadians(v);
            L = L + sun_elements[i][0] * Math.sin(u);
            R = R + sun_elements[i][1] * Math.cos(u);
        }

        double lon = normalizeRadians(4.9353929 + normalizeRadians(62833.196168 * t2) + L / 10000000.0) * RAD_TO_DEG;
        double sdistance = 1.0001026 + R / 10000000.0;

        // Now subtract aberration. Note light-time is not corrected, negligible for Sun
        lon += -.00569;

        double slongitude = lon; // apparent longitude (error<0.001 deg)
        double slatitude = 0; // Sun's ecliptic latitude is always negligible

        return new double[] {slongitude, slatitude, sdistance, Math.atan(696000 / (AU * sdistance))};
    }

    private double[] getMoon() {
        // MOON PARAMETERS (Formulae from "Calendrical Calculations")
        double phase = normalizeRadians((297.8502042 + 445267.1115168 * t - 0.00163 * t * t + t * t * t / 538841 - t * t * t * t / 65194000) * DEG_TO_RAD);

        // Anomalistic phase
        double anomaly = (134.9634114 + 477198.8676313 * t + .008997 * t * t + t * t * t / 69699 - t * t * t * t / 14712000);
        anomaly = anomaly * DEG_TO_RAD;

        // Degrees from ascending node
        double node = (93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000 + t * t * t * t / 863310000);
        node = node * DEG_TO_RAD;

        double E = 1.0 - (.002495 + 7.52E-06 * (t + 1.0)) * (t + 1.0);

        // Solar anomaly
        double sanomaly = (357.5291 + 35999.0503 * t - .0001559 * t * t - 4.8E-07 * t * t * t) * DEG_TO_RAD;

        // Now longitude, with the three main correcting terms of evection,
        // variation, and equation of year, plus other terms (error<0.01 deg)
        // P. Duffet's MOON program taken as reference
        double l = (218.31664563 + 481267.8811958 * t - .00146639 * t * t + t * t * t / 540135.03 - t * t * t * t / 65193770.4);
        l += 6.28875 * Math.sin(anomaly) + 1.274018 * Math.sin(2 * phase - anomaly) + .658309 * Math.sin(2 * phase);
        l +=  0.213616 * Math.sin(2 * anomaly) - E * .185596 * Math.sin(sanomaly) - 0.114336 * Math.sin(2 * node);
        l += .058793 * Math.sin(2 * phase - 2 * anomaly) + .057212 * E * Math.sin(2 * phase - anomaly - sanomaly) + .05332 * Math.sin(2 * phase + anomaly);
        l += .045874 * E * Math.sin(2 * phase - sanomaly) + .041024 * E * Math.sin(anomaly - sanomaly) - .034718 * Math.sin(phase) - E * .030465 * Math.sin(sanomaly + anomaly);
        l += .015326 * Math.sin(2 * (phase - node)) - .012528 * Math.sin(2 * node + anomaly) - .01098 * Math.sin(2 * node - anomaly) + .010674 * Math.sin(4 * phase - anomaly);
        l += .010034 * Math.sin(3 * anomaly) + .008548 * Math.sin(4 * phase - 2 * anomaly);
        l += -E * .00791 * Math.sin(sanomaly - anomaly + 2 * phase) - E * .006783 * Math.sin(2 * phase + sanomaly) + .005162 * Math.sin(anomaly - phase) + E * .005 * Math.sin(sanomaly + phase);
        l += .003862 * Math.sin(4 * phase) + E * .004049 * Math.sin(anomaly - sanomaly + 2 * phase) + .003996 * Math.sin(2 * (anomaly + phase)) + .003665 * Math.sin(2 * phase - 3 * anomaly);
        l += E * 2.695E-3 * Math.sin(2 * anomaly - sanomaly) + 2.602E-3 * Math.sin(anomaly - 2*(node+phase));
        l += E * 2.396E-3 * Math.sin(2*(phase - anomaly) - sanomaly) - 2.349E-3 * Math.sin(anomaly+phase);
        l += E * E * 2.249E-3 * Math.sin(2*(phase-sanomaly)) - E * 2.125E-3 * Math.sin(2*anomaly+sanomaly);
        l += -E * E * 2.079E-3 * Math.sin(2*sanomaly) + E * E * 2.059E-3 * Math.sin(2*(phase-sanomaly)-anomaly);
        l += -1.773E-3 * Math.sin(anomaly+2*(phase-node)) - 1.595E-3 * Math.sin(2*(node+phase));
        l += E * 1.22E-3 * Math.sin(4*phase-sanomaly-anomaly) - 1.11E-3 * Math.sin(2*(anomaly+node));
        double longitude = l;

        // Get accurate Moon age
        double Psin = 29.530588853;
        moonAge = normalizeRadians(longitude * DEG_TO_RAD - sun.eclipticLongitude) * Psin / TWO_PI;

        // Now Moon parallax
        double parallax = .950724 + .051818 * Math.cos(anomaly) + .009531 * Math.cos(2 * phase - anomaly);
        parallax += .007843 * Math.cos(2 * phase) + .002824 * Math.cos(2 * anomaly);
        parallax += 0.000857 * Math.cos(2 * phase + anomaly) + E * .000533 * Math.cos(2 * phase - sanomaly);
        parallax += E * .000401 * Math.cos(2 * phase - anomaly - sanomaly) + E * .00032 * Math.cos(anomaly - sanomaly) - .000271 * Math.cos(phase);
        parallax += -E * .000264 * Math.cos(sanomaly + anomaly) - .000198 * Math.cos(2 * node - anomaly);
        parallax += 1.73E-4 * Math.cos(3 * anomaly) + 1.67E-4 * Math.cos(4*phase-anomaly);

        // So Moon distance in Earth radii is, more or less,
        double distance = 1.0 / Math.sin(parallax * DEG_TO_RAD);

        // Ecliptic latitude with nodal phase (error<0.01 deg)
        l = 5.128189 * Math.sin(node) + 0.280606 * Math.sin(node + anomaly) + 0.277693 * Math.sin(anomaly - node);
        l += .173238 * Math.sin(2 * phase - node) + .055413 * Math.sin(2 * phase + node - anomaly);
        l += .046272 * Math.sin(2 * phase - node - anomaly) + .032573 * Math.sin(2 * phase + node);
        l += .017198 * Math.sin(2 * anomaly + node) + .009267 * Math.sin(2 * phase + anomaly - node);
        l += .008823 * Math.sin(2 * anomaly - node) + E * .008247 * Math.sin(2 * phase - sanomaly - node) + .004323 * Math.sin(2 * (phase - anomaly) - node);
        l += .0042 * Math.sin(2 * phase + node + anomaly) + E * .003372 * Math.sin(node - sanomaly - 2 * phase);
        l += E * 2.472E-3 * Math.sin(2 * phase + node - sanomaly - anomaly);
        l += E * 2.222E-3 * Math.sin(2 * phase + node - sanomaly);
        l += E * 2.072E-3 * Math.sin(2 * phase - node - sanomaly - anomaly);
        double latitude = l;

        return new double[] {longitude, latitude, distance * EARTH_RADIUS / AU, Math.atan(1737.4 / (distance * EARTH_RADIUS))};
    }

    private Ephemeris doCalc(double[] pos, boolean geocentric) {
        // Correct for nutation in longitude and obliquity
        double M1 = (124.90 - 1934.134 * t + 0.002063 * t * t) * DEG_TO_RAD;
        double M2 = (201.11 + 72001.5377 * t + 0.00057 * t * t) * DEG_TO_RAD;
        double dLon = - .0047785 * Math.sin(M1) - .0003667 * Math.sin(M2);
        double dLat = .002558 * Math.cos(M1) - .00015339 * Math.cos(M2);
        pos[0] += dLon;
        pos[1] += dLat;

        // Ecliptic to equatorial coordinates
        double t2 = this.t / 100.0;
        double tmp = t2 * (27.87 + t2 * (5.79 + t2 * 2.45));
        tmp = t2 * (-249.67 + t2 * (-39.05 + t2 * (7.12 + tmp)));
        tmp = t2 * (-1.55 + t2 * (1999.25 + t2 * (-51.38 + tmp)));
        tmp = (t2 * (-4680.93 + tmp)) / 3600.0;
        double angle = (23.4392911111111 + tmp) * DEG_TO_RAD; // mean obliquity

        pos[0] *= DEG_TO_RAD;
        pos[1] *= DEG_TO_RAD;
        double cl = Math.cos(pos[1]);
        double x = pos[2] * Math.cos(pos[0]) * cl;
        double y = pos[2] * Math.sin(pos[0]) * cl;
        double z = pos[2] * Math.sin(pos[1]);
        tmp = y * Math.cos(angle) - z * Math.sin(angle);
        z = y * Math.sin(angle) + z * Math.cos(angle);
        y = tmp;

        if (geocentric) return new Ephemeris(0, 0, -1, -1, -1, -1, normalizeRadians(Math.atan2(y, x)),
                Math.atan2(z / Math.sqrt(x * x + y * y), 1.0), 	Math.sqrt(x * x + y * y + z * z), pos[0], pos[1], pos[3]);

        // Obtain local apparent sidereal time
        double jd0 = Math.floor(jd_UT - 0.5) + 0.5;
        double T0 = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY;
        double secs = (jd_UT - jd0) * SECONDS_PER_DAY;
        double gmst = (((((-6.2e-6 * T0) + 9.3104e-2) * T0) + 8640184.812866) * T0) + 24110.54841;
        double msday = 1.0 + (((((-1.86e-5 * T0) + 0.186208) * T0) + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY));
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD;
        double lst = gmst + obsLon;

        // Obtain topocentric rectangular coordinates
        double radiusAU = EARTH_RADIUS / AU;
        double correction[] = new double[] {
                radiusAU * Math.cos(obsLat) * Math.cos(lst),
                radiusAU * Math.cos(obsLat) * Math.sin(lst),
                radiusAU * Math.sin(obsLat)};
        double xtopo = x - correction[0];
        double ytopo = y - correction[1];
        double ztopo = z - correction[2];

        // Obtain topocentric equatorial coordinates
        double ra = 0.0;
        double dec = PI_OVER_TWO;
        if (ztopo < 0.0) dec = -dec;
        if (ytopo != 0.0 || xtopo != 0.0) {
            ra = Math.atan2(ytopo, xtopo);
            dec = Math.atan2(ztopo / Math.sqrt(xtopo * xtopo + ytopo * ytopo), 1.0);
        }
        double dist = Math.sqrt(xtopo * xtopo + ytopo * ytopo + ztopo * ztopo);

        // Hour angle
        double angh = lst - ra;

        // Obtain azimuth and geometric alt
        double sinlat = Math.sin(obsLat);
        double coslat = Math.cos(obsLat);
        double sindec = Math.sin(dec), cosdec = Math.cos(dec);
        double h = sinlat * sindec + coslat * cosdec * Math.cos(angh);
        double alt = Math.asin(h);
        double azy = Math.sin(angh);
        double azx = Math.cos(angh) * sinlat - sindec * coslat / cosdec;
        double azi = Math.PI + Math.atan2(azy, azx); // 0 = north

        // Get apparent elevation
        if (alt > -3 * DEG_TO_RAD) {
            double r = 0.016667 * DEG_TO_RAD * Math.abs(Math.tan(PI_OVER_TWO - (alt * RAD_TO_DEG +  7.31 / (alt * RAD_TO_DEG + 4.4)) * DEG_TO_RAD));
            double refr = r * ( 0.28 * 1010 / (10 + 273.0)); // Assuming pressure of 1010 mb and T = 10 C
            alt = Math.min(alt + refr, PI_OVER_TWO); // This is not accurate, but acceptable
        }

        switch (twilight) {
            case HORIZON_34arcmin:
                // Rise, set, transit times, taking into account Sun/Moon angular radius (pos[3]).
                // The 34' factor is the standard refraction at horizon.
                // Removing angular radius will do calculations for the center of the disk instead
                // of the upper limb.
                tmp = -(34.0 / 60.0) * DEG_TO_RAD - pos[3];
                break;
            case TWILIGHT_CIVIL:
                tmp = -6 * DEG_TO_RAD;
                break;
            case TWILIGHT_NAUTICAL:
                tmp = -12 * DEG_TO_RAD;
                break;
            case TWILIGHT_ASTRONOMICAL:
                tmp = -18 * DEG_TO_RAD;
                break;
        }

        // Compute cosine of hour angle
        tmp = (Math.sin(tmp) - Math.sin(obsLat) * Math.sin(dec)) / (Math.cos(obsLat) * Math.cos(dec));
        /** Length of a sidereal day in days according to IERS Conventions. */
        double siderealDayLength = 1.00273781191135448;
        double celestialHoursToEarthTime = 1.0 / (siderealDayLength * TWO_PI);

        // Make calculations for the meridian
        double transit_time1 = celestialHoursToEarthTime * normalizeRadians(ra - lst);
        double transit_time2 = celestialHoursToEarthTime * (normalizeRadians(ra - lst) - TWO_PI);
        double transit_alt = Math.asin(Math.sin(dec) * Math.sin(obsLat) + Math.cos(dec) * Math.cos(obsLat));
        if (transit_alt > -3 * DEG_TO_RAD) {
            double r = 0.016667 * DEG_TO_RAD * Math.abs(Math.tan(PI_OVER_TWO - (transit_alt * RAD_TO_DEG +  7.31 / (transit_alt * RAD_TO_DEG + 4.4)) * DEG_TO_RAD));
            double refr = r * ( 0.28 * 1010 / (10 + 273.0)); // Assuming pressure of 1010 mb and T = 10 C
            transit_alt = Math.min(transit_alt + refr, PI_OVER_TWO); // This is not accurate, but acceptable
        }

        // Obtain the current event in time
        double transit_time = transit_time1;
        double jdToday = Math.floor(jd_UT - 0.5) + 0.5;
        double transitToday2 = Math.floor(jd_UT + transit_time2 - 0.5) + 0.5;
        // Obtain the transit time. Preference should be given to the closest event
        // in time to the current calculation time
        if (jdToday == transitToday2 && Math.abs(transit_time2) < Math.abs(transit_time1)) transit_time = transit_time2;
        double transit = jd_UT + transit_time;

        // Make calculations for rise and set
        double rise = -1, set = -1;
        if (Math.abs(tmp) <= 1.0) {
            double ang_hor = Math.abs(Math.acos(tmp));
            double rise_time1 = celestialHoursToEarthTime * normalizeRadians(ra - ang_hor - lst);
            double set_time1 = celestialHoursToEarthTime * normalizeRadians(ra + ang_hor - lst);
            double rise_time2 = celestialHoursToEarthTime * (normalizeRadians(ra - ang_hor - lst) - TWO_PI);
            double set_time2 = celestialHoursToEarthTime * (normalizeRadians(ra + ang_hor - lst) - TWO_PI);

            // Obtain the current events in time. Preference should be given to the closest event
            // in time to the current calculation time (so that iteration in other method will converge)
            double rise_time = rise_time1;
            double riseToday2 = Math.floor(jd_UT + rise_time2 - 0.5) + 0.5;
            if (jdToday == riseToday2 && Math.abs(rise_time2) < Math.abs(rise_time1)) rise_time = rise_time2;

            double set_time = set_time1;
            double setToday2 = Math.floor(jd_UT + set_time2 - 0.5) + 0.5;
            if (jdToday == setToday2 && Math.abs(set_time2) < Math.abs(set_time1)) set_time = set_time2;
            rise = jd_UT + rise_time;
            set = jd_UT + set_time;
        }

        Ephemeris out = new Ephemeris(azi, alt, rise, set, transit, transit_alt,
                normalizeRadians(ra), dec, dist, pos[0], pos[1], pos[3]);
        return out;
    }

    /**
     * Transforms a Julian day (rise/set/transit fields) to a common date.
     * @param jd The Julian day.
     * @return A set of integers: year, month, day, hour, minute, second.
     * @throws Exception If the input date does not exists.
     */
    public static int[] getDate(double jd) throws Exception {
        if (jd < 2299160.0 && jd >= 2299150.0)
            throw new Exception("invalid julian day " + jd + ". This date does not exist.");

        // The conversion formulas are from Meeus,
        // Chapter 7.
        double Z = Math.floor(jd + 0.5);
        double F = jd + 0.5 - Z;
        double A = Z;
        if (Z >= 2299161D) {
            int a = (int) ((Z - 1867216.25) / 36524.25);
            A += 1 + a - a / 4;
        }
        double B = A + 1524;
        int C = (int) ((B - 122.1) / 365.25);
        int D = (int) (C * 365.25);
        int E = (int) ((B - D) / 30.6001);

        double exactDay = F + B - D - (int) (30.6001 * E);
        int day = (int) exactDay;
        int month = (E < 14) ? E - 1 : E - 13;
        int year = C - 4715;
        if (month > 2) year--;
        double h = ((exactDay - day) * SECONDS_PER_DAY) / 3600.0;

        int hour = (int) h;
        double m = (h - hour) * 60.0;
        int minute = (int) m;
        int second = (int) ((m - minute) * 60.0);

        return new int[] {year, month, day, hour, minute, second};
    }

    /**
     * Returns a date as a string.
     * @param jd The Julian day.
     * @return The String.
     * @throws Exception If the date does not exists.
     */
    public static String getDateAsString(double jd) throws Exception {
        if (jd == -1) return "NO RISE/SET/TRANSIT FOR THIS OBSERVER/DATE";

        int date[] = SunMoonCalculator.getDate(jd);
        String zyr = "", zmo = "", zh = "", zm = "", zs = "";
        if (date[1] < 10) zyr = "0";
        if (date[2] < 10) zmo = "0";
        if (date[3] < 10) zh = "0";
        if (date[4] < 10) zm = "0";
        if (date[5] < 10) zs = "0";
        return date[0]+"/"+zyr+date[1]+"/"+zmo+date[2]+" "+zh+date[3]+":"+zm+date[4]+":"+zs+date[5]+" UT";
    }

    /**
     * Reduce an angle in radians to the range (0 - 2 Pi).
     * @param r Value in radians.
     * @return The reduced radians value.
     */
    public static double normalizeRadians(double r)
    {
        if (r < 0 && r >= -TWO_PI) return r + TWO_PI;
        if (r >= TWO_PI && r < 2*TWO_PI) return r - TWO_PI;
        if (r >= 0 && r < TWO_PI) return r;

        r -= TWO_PI * Math.floor(r / TWO_PI);
        if (r < 0.) r += TWO_PI;

        return r;
    }

    private double obtainAccurateRiseSetTransit(double riseSetJD, EVENT index, int niter, boolean sun) {
        double step = -1;
        for (int i = 0; i< niter; i++) {
            if (riseSetJD == -1) return riseSetJD; // -1 means no rise/set from that location
            setUTDate(riseSetJD);
            Ephemeris out = null;
            if (sun) {
                out = doCalc(getSun(), false);
            } else {
                getSun();
                out = doCalc(getMoon(), false);
            }

            double val = out.rise;
            if (index == EVENT.SET) val = out.set;
            if (index == EVENT.TRANSIT) val = out.transit;
            step = Math.abs(riseSetJD - val);
            riseSetJD = val;
        }
        if (step > 1.0 / SECONDS_PER_DAY) return -1; // did not converge => without rise/set/transit in this date
        return riseSetJD;
    }

    public double[] getMoonDiskOrientationAngles()
    {
        sun = doCalc(getSun(), false);
        double moonPos[] = getMoon();
        moon = doCalc(moonPos, false);
        double moonLon = moonPos[0], moonLat = moonPos[1],
                moonRA = moon.rightAscension, moonDEC = moon.declination;
        double sunRA = sun.rightAscension, sunDEC = sun.declination;

        // Moon's argument of latitude
        double F = (93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000.0 + t * t * t * t / 863310000.0) * DEG_TO_RAD;
        // Moon's inclination
        double I = 1.54242 * DEG_TO_RAD;
        // Moon's mean ascending node longitude
        double omega = (125.0445550 - 1934.1361849 * t + 0.0020762 * t * t + t * t * t / 467410.0 - t * t * t * t / 18999000.0) * DEG_TO_RAD;
        // Obliquity of ecliptic (approx, better formulae up)
        double eps = 23.43929 * DEG_TO_RAD;

        // Obtain optical librations lp and bp
        double W = moonLon - omega;
        double sinA = Math.sin(W) * Math.cos(moonLat) * Math.cos(I) - Math.sin(moonLat) * Math.sin(I);
        double cosA = Math.cos(W) * Math.cos(moonLat);
        double A = Math.atan2(sinA, cosA);
        double lp = normalizeRadians(A - F);
        double sinbp = -Math.sin(W) * Math.cos(moonLat) * Math.sin(I) - Math.sin(moonLat) * Math.cos(I);
        double bp = Math.asin(sinbp);

        // Obtain position angle of axis p
        double x = Math.sin(I) * Math.sin(omega);
        double y = Math.sin(I) * Math.cos(omega) * Math.cos(eps) - Math.cos(I) * Math.sin(eps);
        double w = Math.atan2(x, y);
        double sinp = Math.sqrt(x * x + y * y) * Math.cos(moonRA - w) / Math.cos(bp);
        double p = Math.asin(sinp);

        // Compute bright limb angle bl
        double bl = (Math.PI + Math.atan2(Math.cos(sunDEC) * Math
                .sin(moonRA - sunRA), Math.cos(sunDEC) * Math
                .sin(moonDEC) * Math.cos(moonRA - sunRA) - Math
                .sin(sunDEC) * Math.cos(moonDEC)));

        // Paralactic angle par (first obtain local apparent sidereal time)
        double jd0 = Math.floor(jd_UT - 0.5) + 0.5;
        double T0 = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY;
        double secs = (jd_UT - jd0) * SECONDS_PER_DAY;
        double gmst = (((((-6.2e-6 * T0) + 9.3104e-2) * T0) + 8640184.812866) * T0) + 24110.54841;
        double msday = 1.0 + (((((-1.86e-5 * T0) + 0.186208) * T0) + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY));
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD;
        double lst = gmst + obsLon;

        y = Math.sin(lst - moonRA);
        x = Math.tan(obsLat) * Math.cos(moonDEC) - Math.sin(moonDEC) * Math.cos(lst - moonRA);
        double par = 0.0;
        if (x != 0.0)
        {
            par = Math.atan2(y, x);
        }
        else
        {
            par = (y / Math.abs(y)) * PI_OVER_TWO;
        }
        return new double[]{lp, bp, p, bl, par};
    }

    /**
     * Main test program.
     * @param args Not used
     */
    public static void main(String[] args) {
        System.out.println("SunMoonCalculator test run");

        try {
            int year = 2018, month = 7, day = 28, h = 16-2, m = 28, s = 52; // in UT !!!
            double obsLon = -3.7 * DEG_TO_RAD, obsLat = 40.417 * DEG_TO_RAD; // lon is negative to the west
            SunMoonCalculator smc = new SunMoonCalculator(year, month, day, h, m, s, obsLon, obsLat);

            smc.calcSunAndMoon();

            String degSymbol = "\u00b0";
            System.out.println("Sun");
            System.out.println(" Az:       "+(float) (smc.sun.azimuth * RAD_TO_DEG)+degSymbol);
            System.out.println(" El:       "+(float) (smc.sun.elevation * RAD_TO_DEG)+degSymbol);
            System.out.println(" Dist:     "+(float) (smc.sun.distance)+" AU");
            System.out.println(" RA:       "+(float) (smc.sun.rightAscension * RAD_TO_DEG)+degSymbol);
            System.out.println(" DEC:      "+(float) (smc.sun.declination * RAD_TO_DEG)+degSymbol);
            System.out.println(" Ill:      "+(float) (smc.sun.illuminationPhase)+"%");
            System.out.println(" ang.R:    "+(float) (smc.sun.angularRadius * RAD_TO_DEG)+degSymbol);
            System.out.println(" Rise:     "+SunMoonCalculator.getDateAsString(smc.sun.rise));
            System.out.println(" Set:      "+SunMoonCalculator.getDateAsString(smc.sun.set));
            System.out.println(" Transit:  "+SunMoonCalculator.getDateAsString(smc.sun.transit)+" (elev. "+(float) (smc.sun.transitElevation * RAD_TO_DEG)+degSymbol+")");
			/*
			System.out.println(" Az=+angR: "+SunMoonCalculator.getDateAsString(smc.getAzimuthTime(true, Math.PI+smc.sun.angularRadius)));
			System.out.println(" Max Elev: "+SunMoonCalculator.getDateAsString(smc.getCulminationTime(true, false)));
			System.out.println(" Az=0:     "+SunMoonCalculator.getDateAsString(smc.getAzimuthTime(true, 0)));
			System.out.println(" Min Elev: "+SunMoonCalculator.getDateAsString(smc.getCulminationTime(true, true)));
			*/
            System.out.println("Moon");
            System.out.println(" Az:       "+(float) (smc.moon.azimuth * RAD_TO_DEG)+degSymbol);
            System.out.println(" El:       "+(float) (smc.moon.elevation * RAD_TO_DEG)+degSymbol);
            System.out.println(" Dist:     "+(float) (smc.moon.distance * AU)+" km");
            System.out.println(" RA:       "+(float) (smc.moon.rightAscension * RAD_TO_DEG)+degSymbol);
            System.out.println(" DEC:      "+(float) (smc.moon.declination * RAD_TO_DEG)+degSymbol);
            System.out.println(" Ill:      "+(float) (smc.moon.illuminationPhase)+"%");
            System.out.println(" ang.R:    "+(float) (smc.moon.angularRadius * RAD_TO_DEG)+degSymbol);
            System.out.println(" Age:      "+(float) (smc.moonAge)+" days");
            System.out.println(" Rise:     "+SunMoonCalculator.getDateAsString(smc.moon.rise));
            System.out.println(" Set:      "+SunMoonCalculator.getDateAsString(smc.moon.set));
            System.out.println(" Transit:  "+SunMoonCalculator.getDateAsString(smc.moon.transit)+" (elev. "+(float) (smc.moon.transitElevation * RAD_TO_DEG)+degSymbol+")");
			/*
			System.out.println(" Az=+angR: "+SunMoonCalculator.getDateAsString(smc.getAzimuthTime(false, Math.PI+smc.moon.angularRadius)));
			System.out.println(" Max Elev: "+SunMoonCalculator.getDateAsString(smc.getCulminationTime(false, false)));
			System.out.println(" Az=0:     "+SunMoonCalculator.getDateAsString(smc.getAzimuthTime(false, 0)));
			System.out.println(" Min Elev: "+SunMoonCalculator.getDateAsString(smc.getCulminationTime(false, true)));
			*/

            smc.setTwilight(TWILIGHT.TWILIGHT_ASTRONOMICAL);
            smc.calcSunAndMoon();

            System.out.println("");
            System.out.println("Astronomical twilights:");
            System.out.println("Sun");
            System.out.println(" Rise:     "+SunMoonCalculator.getDateAsString(smc.sun.rise));
            System.out.println(" Set:      "+SunMoonCalculator.getDateAsString(smc.sun.set));
            System.out.println("Moon");
            System.out.println(" Rise:     "+SunMoonCalculator.getDateAsString(smc.moon.rise));
            System.out.println(" Set:      "+SunMoonCalculator.getDateAsString(smc.moon.set));

			/*
			System.out.println("");
			System.out.println("Closest Moon phases:");
			for (int i=0; i<MOONPHASE.values().length; i++) {
				MOONPHASE mp = MOONPHASE.values()[i];
				System.out.println(" "+mp.phaseName+"  "+SunMoonCalculator.getDateAsString(smc.getMoonPhaseTime(mp)));
			}

			double equinox[] = smc.getEquinoxes();
			double solstices[] = smc.getSolstices();
			System.out.println("");
			System.out.println("Equinoxes and solstices:");
			System.out.println(" Spring equinox:    "+SunMoonCalculator.getDateAsString(equinox[0]));
			System.out.println(" Autumn equinox:    "+SunMoonCalculator.getDateAsString(equinox[1]));
			System.out.println(" Summer solstice:   "+SunMoonCalculator.getDateAsString(solstices[0]));
			System.out.println(" Winter solstice:   "+SunMoonCalculator.getDateAsString(solstices[1]));
			*/

            // Expected accuracy over 1800 - 2200:
            // - Sun: 0.001 deg in RA/DEC, 0.003 deg or 10 arcsec in Az/El.
            //        <1s in rise/set/transit times. 1 min in Equinoxes/Solstices
            // - Mon: 0.03 deg or better.
            //        10s or better in rise/set/transit times. 2 minutes in lunar phases.
            //        In most cases the actual accuracy in the Moon will be better, but it is not guaranteed.
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}