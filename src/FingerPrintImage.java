import java.util.ArrayList;
import java.util.List;

public class FingerPrintImage {
	private int width;
	private int height;
	private char[][] img;
	// Práctica 2
	private int valorMin;
	
	// Práctica 5
	public List<Minutia> listaMinutias = new ArrayList<>();
	
	public int getValorMin() {
		return valorMin;
	}

	public void setValorMin(int valorMin) {
		this.valorMin = valorMin;
	}

	public int getValorMax() {
		return valorMax;
	}

	public void setValorMax(int valorMax) {
		this.valorMax = valorMax;
	}

	public int getValorMedio() {
		return valorMedio;
	}

	public void setValorMedio(int valorMedio) {
		this.valorMedio = valorMedio;
	}

	private int valorMax;
	private int valorMedio;

	FingerPrintImage(int width, int height) {
		this.height = height;
		this.width = width;
		img = new char[width][height];
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setPixel(int x, int y, char color) {
		img[x][y] = color;
	}

	public char getPixel(int x, int y) {
		return img[x][y];
	}
}
