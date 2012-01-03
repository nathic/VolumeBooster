/*************************************************************************
 *  Datei:        Complex.java
 *  Compilieren:  javac Complex.java
 *  Ausfuehren:   java Complex
 *
 *  Daten-Typen fuer komplexe Zahlen
 *  implementiert von Sebastian Fuchs
 *
 *************************************************************************/

// Loesung von Blatt 3, Aufgabe 3(a)
public class Complex
{

    private final double re;	// Realteil und
    private final double im;	// Imaginaerteil - koennen nicht veraendert werden

    // Konstruktor: Erzeuge neues Objekt mit gegebenem Real- und Imaginaerteil
    public Complex(double a, double b)
    {
	re = a;
	im = b;
    }

    // Konstruktor: Erzeuge neues Objekt mit gegebenem Realteil, Imaginaerteil=0
    public Complex(double r )
    {
	re = r;
	im = 0.;
    }

    // Konvertiere komplexe Zahl in String
    public String toString() {
        if (im == 0) return Double.toString(re);
        if (re == 0) return im + " I";
        if (im <  0) return re + " - " + (-im) + " I";
        return re + " + " + im + " I";
    }

    // Zugriff von ausserhalb auf Realteil
    public double Real()
    {
	return re;
    }

    // Zugriff von ausserhalb auf Imaginaerteil
    public double Imag()
    {
	return im;
    }

    // Absolutbetrag der komplexen Zyahl
    public double abs()
    {
	return Math.sqrt(re*re+im*im);
    }

    // Erzeuge ein neues Objekt mit Wert (this + r)
    public Complex plus(Complex r)
    {
	return new Complex(re+r.re, im+r.im); 
    }

    // Erzeuge ein neues Objekt mit Wert (this - r)
    public Complex minus(Complex r)
    {
	return new Complex(re-r.re, im-r.im);
    }

    // Erzeuge ein neues Objekt mit Wert (this * r)
    public Complex times(Complex r)
    {
	return new Complex(re*r.re-im*r.im, re*r.im+im*r.re); 
    }

    // Erzeuge ein neues Objekt mit Wert (this * d), wobei d reell ist
    public Complex times(double d)
    {
	return new Complex(re*d, im*d);
    }

    // Erzeuge ein neues Objekt mit Wert (this / r)
    public Complex divide(Complex r)
    {
	// Verwende this/r = r^* * this /|r|^2
	return this.times(r.conjugate()).divide(r.re*r.re+r.im*r.im);
    }

    // Erzeuge ein neues Objekt mit Wert (this / d), wobei d reell ist
    public Complex divide(double d)
    {
        if(d == 0)
	   System.err.println("### Complex: Division durch 0");
	return new Complex(re/d, im/d);
    }

    // Erzeuge ein neues Objekt mit Wert this^*
    public Complex conjugate()
    {
	return new Complex(re, -im);
    }
}