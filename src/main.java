import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Point;
import java.util.*;
import java.lang.*;

import javax.imageio.ImageIO;

public class byss1 {
	final static int[][] nbrs = { { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 },
			{ 0, -1 } };

	final static int[][][] nbrGroups = { { { 0, 2, 4 }, { 2, 4, 6 } }, { { 0, 2, 6 }, { 0, 4, 6 } } };

	static List<Point> aBorrar = new ArrayList<>();

	static int numNeighbors(int r, int c, FingerPrintImage entrada) {
		int count = 0;
		for (int i = 0; i < nbrs.length - 1; i++)
			if (entrada.getPixel(c + nbrs[i][0], r + nbrs[i][1]) == 1)
				count++;
		return count;
	}

	static int numTransitions(int r, int c, FingerPrintImage entrada) {
		int count = 0;
		for (int i = 0; i < nbrs.length - 1; i++)
			if (entrada.getPixel(c + nbrs[i][0], r + nbrs[i][1]) == 0) {
				if (entrada.getPixel(c + nbrs[i + 1][0], r + nbrs[i + 1][1]) == 1)
					count++;
			}
		return count;
	}

	static boolean atLeastOneIsWhite(int r, int c, int step, FingerPrintImage entrada) {
		int count = 0;
		int[][] group = nbrGroups[step];
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < group[i].length; j++) {
				int[] nbr = nbrs[group[i][j]];
				if (entrada.getPixel(c + nbr[0], r + nbr[1]) == 0) {
					count++;
					break;
				}
			}
		return count > 1;
	}

	static FingerPrintImage thinImage(FingerPrintImage entrada) {
		boolean firstStep = false;
		boolean hasChanged;

		do {
			hasChanged = false;
			firstStep = !firstStep;

			for (int r = 1; r < entrada.getHeight() - 1; r++) {
				for (int c = 1; c < entrada.getWidth() - 1; c++) {

					if (entrada.getPixel(c, r) != 1)
						continue;

					int nn = numNeighbors(r, c, entrada);
					if (nn < 2 || nn > 6)
						continue;

					if (numTransitions(r, c, entrada) != 1)
						continue;

					if (!atLeastOneIsWhite(r, c, firstStep ? 0 : 1, entrada))
						continue;

					aBorrar.add(new Point(c, r));
					hasChanged = true;
				}
			}

			for (Point p : aBorrar)
				entrada.setPixel(p.x, p.y, (char) 0);
			// grid[p.y][p.x] = ' ';
			aBorrar.clear();

		} while (firstStep || hasChanged);

		return entrada;
	}

	static BufferedImage cargarImagen(String path) throws IOException {
		BufferedImage imagenentrada = null;
		imagenentrada = ImageIO.read(new File(path));
		System.out.println("Cargada la imagen");
		return imagenentrada;
	}

	static void guardarImagen(BufferedImage imagenentrada, String path) throws IOException {
		File outputfile = new File(path);
		ImageIO.write(imagenentrada, "png", outputfile);
	}

	static FingerPrintImage transformarGrises(BufferedImage imagenentrada) {
		System.out.println("Conviertiendo a grises...");

		FingerPrintImage imagensalida = new FingerPrintImage(imagenentrada.getWidth(), imagenentrada.getHeight());

		for (int x = 0; x < imagenentrada.getWidth(); ++x) {
			for (int y = 0; y < imagenentrada.getHeight(); ++y) {
				int rgb = imagenentrada.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb & 0xFF);
				int nivelGris = (r + g + b) / 3;
				imagensalida.setPixel(x, y, (char) nivelGris);
			}
		}
		return imagensalida;
	}

	static FingerPrintImage ecualizarImagen(FingerPrintImage imagenentrada) {
		System.out.println("Ecualizando...");

		int width = imagenentrada.getWidth();
		int height = imagenentrada.getHeight();
		int tampixel = width * height;
		int[] histograma = new int[256]; // contador que cuenta el número de ocurrencia de cada nivel de gris (256
											// niveles)
		int i = 0;
		FingerPrintImage imagenecualizada = new FingerPrintImage(width, height);

		// Calculamos frecuencia relativa de ocurrencia
		// de los distintos niveles de gris en la imagen
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int valor = imagenentrada.getPixel(x, y);
				histograma[valor]++;
			}
		}
		int sum = 0;
		// Construimos la Lookup table LUT
		float[] lut = new float[256];
		for (i = 0; i < 256; ++i) {
			sum += histograma[i];
			lut[i] = sum * 255 / tampixel;
		}
		// Se transforma la imagen utilizando la tabla LUT
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int valor = imagenentrada.getPixel(x, y);
				int valorNuevo = (int) lut[valor];
				imagenecualizada.setPixel(x, y, (char) valorNuevo);
			}
		}
		return imagenecualizada;
	}

	static BufferedImage transformarRGB(FingerPrintImage imagenentrada, int modo) {
		BufferedImage imagensalida = new BufferedImage(imagenentrada.getWidth(), imagenentrada.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < imagenentrada.getWidth(); ++x) {
			for (int y = 0; y < imagenentrada.getHeight(); ++y) {
				int valor = imagenentrada.getPixel(x, y);
				if (modo == 0) {
					valor = valor * 255;
				}
				int pixelRGB = (255 << 24 | valor << 16 | valor << 8 | valor);
				imagensalida.setRGB(x, y, pixelRGB);
			}
		}
		return imagensalida;
	}

	static BufferedImage pintarMinutias(FingerPrintImage imagenentrada, int modo) {
		BufferedImage imagensalida = new BufferedImage(imagenentrada.getWidth(), imagenentrada.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < imagenentrada.getWidth(); ++x) {
			for (int y = 0; y < imagenentrada.getHeight(); ++y) {
				int valor = imagenentrada.getPixel(x, y);
				if (modo == 0) {
					valor = valor * 255;
				}
				int pixelRGB = (255 << 24 | valor << 16 | valor << 8 | valor);
				imagensalida.setRGB(x, y, pixelRGB);
			}
		}

		int pixelMinutia, x, y;
		for (int i = 0; i < imagenentrada.listaMinutias.size(); i++) {
			pixelMinutia = 255;
			x = imagenentrada.listaMinutias.get(i).x;
			y = imagenentrada.listaMinutias.get(i).y;

			if (imagenentrada.listaMinutias.get(i).tipo == 1) {
				pixelMinutia = (255 << 24 | 255 << 16 | 0 << 8 | 0);
			} else if (imagenentrada.listaMinutias.get(i).tipo == 3) {
				pixelMinutia = (255 << 24 | 0 << 16 | 0 << 8 | 255);
			}

			imagensalida.setRGB(x, y, pixelMinutia);
			imagensalida.setRGB(x - 1, y - 1, pixelMinutia);
			imagensalida.setRGB(x, y - 1, pixelMinutia);
			imagensalida.setRGB(x + 1, y - 1, pixelMinutia);
			imagensalida.setRGB(x - 1, y, pixelMinutia);
			imagensalida.setRGB(x + 1, y, pixelMinutia);
			imagensalida.setRGB(x - 1, y + 1, pixelMinutia);
			imagensalida.setRGB(x, y + 1, pixelMinutia);
			imagensalida.setRGB(x + 1, y + 1, pixelMinutia);

		}
		return imagensalida;
	}

	public static void calcularValores(FingerPrintImage entrada) {
		int width = entrada.getWidth();
		int height = entrada.getHeight();
		int tampixel = width * height;

		int min = 255;
		int max = 0;
		int media = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int valor = entrada.getPixel(x, y);
				media += valor;
				if (valor > max) {
					max = valor;
				}
				if (valor < min) {
					min = valor;
				}
			}
		}
		media = media / tampixel;
		System.out.println("----------------\nValor máximo: " + max + "\nValor mínimo: " + min + "\nValor medio: "
				+ media + "\n----------------");
	}

	static FingerPrintImage convertirBlancoNegro(FingerPrintImage imagenentrada, int umbral) {
		System.out.println("Transformando a Blanco y Negro...");

		int width = imagenentrada.getWidth();
		int height = imagenentrada.getHeight();
		FingerPrintImage imagenFinal = new FingerPrintImage(width, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (imagenentrada.getPixel(x, y) >= umbral) {
					imagenFinal.setPixel(x, y, (char) 0);
				} else {
					imagenFinal.setPixel(x, y, (char) 1);
				}
			}
		}

		return imagenFinal;
	}

	static FingerPrintImage filtrarImagen(FingerPrintImage imagenentrada) {
		System.out.println("Aplicando filtros a la imagen...");

		int width = imagenentrada.getWidth();
		int height = imagenentrada.getHeight();
		FingerPrintImage imagenFinal = new FingerPrintImage(width, height);
		FingerPrintImage imagenFinal2 = new FingerPrintImage(width, height);

		int valorFiltrado;

		// Filtro 1
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				valorFiltrado = imagenentrada.getPixel(x, y)
						| imagenentrada.getPixel(x, y - 1) & imagenentrada.getPixel(x, y + 1)
								& (imagenentrada.getPixel(x - 1, y) | imagenentrada.getPixel(x + 1, y))
						| imagenentrada.getPixel(x - 1, y) & imagenentrada.getPixel(x + 1, y)
								& (imagenentrada.getPixel(x, y - 1) | imagenentrada.getPixel(x, y + 1));

				imagenFinal.setPixel(x, y, (char) valorFiltrado);
			}
		}

		// Filtro 2
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				valorFiltrado = imagenFinal.getPixel(x, y) & ((imagenFinal.getPixel(x - 1, y - 1)
						| imagenFinal.getPixel(x, y - 1) | imagenFinal.getPixel(x - 1, y))
						& (imagenFinal.getPixel(x + 1, y) | imagenFinal.getPixel(x, y + 1)
								| imagenFinal.getPixel(x + 1, y + 1))
						| (imagenFinal.getPixel(x, y - 1) | imagenFinal.getPixel(x + 1, y - 1)
								| imagenFinal.getPixel(x + 1, y))
								& (imagenFinal.getPixel(x - 1, y) | imagenFinal.getPixel(x - 1, y + 1)
										| imagenFinal.getPixel(x, y + 1)));

				imagenFinal2.setPixel(x, y, (char) valorFiltrado);
			}
		}

		return imagenFinal2;
	}

	static int detectarMinutia(int x, int y, FingerPrintImage entrada) {
		int cn = 0;
		int[] p = new int[9];
		p[0] = entrada.getPixel(x - 1, y - 1);
		p[1] = entrada.getPixel(x, y - 1);
		p[2] = entrada.getPixel(x + 1, y - 1);
		p[3] = entrada.getPixel(x + 1, y);
		p[4] = entrada.getPixel(x + 1, y + 1);
		p[5] = entrada.getPixel(x, y + 1);
		p[6] = entrada.getPixel(x - 1, y + 1);
		p[7] = entrada.getPixel(x - 1, y);
		p[8] = entrada.getPixel(x - 1, y - 1);

		for (int i = 0; i < 8; i++) {
			cn = cn + Math.abs(p[i] - p[i + 1]);
		}

		return cn / 2;
	}

	static void calcularMinutias(FingerPrintImage entrada) {
		System.out.println("Calculando Minutias...");

		int cn;
		for (int x = 20; x < entrada.getWidth() - 20; ++x) {
			for (int y = 20; y < entrada.getHeight() - 20; ++y) {
				if (entrada.getPixel(x, y) != 1) {
					continue;
				}
				cn = detectarMinutia(x, y, entrada);

				if (cn == 1 || cn == 3) {
					entrada.listaMinutias.add(new Minutia(x, y, cn, ""));
				}
			}
		}
	}

	public static void calcularAngulos(FingerPrintImage entrada) {
		boolean salir = false;
		int xBase, yBase, x, y;
		
		ArrayList<Point> visitados = new ArrayList<Point>();
		ArrayList<Point> vecinos = new ArrayList<Point>();
		ArrayList<Point> aux = new ArrayList<Point>();

		for (int i = 0; i < entrada.listaMinutias.size(); i++) {

			xBase = entrada.listaMinutias.get(i).x;
			yBase = entrada.listaMinutias.get(i).y;
			x = xBase;
			y = yBase;

			visitados.clear();
			visitados.add(new Point(x, y)); // Guarda coords de la minucia

			getVecinos(vecinos, visitados, vecinos, entrada, x, y);
			while (!vecinos.isEmpty()) { // Recorrer aristas para calcular gradiente

				x = vecinos.get(0).x;
				y = vecinos.get(0).y;

				vecinos.remove(0);

				for (int j = 0; j < 6 && !salir; j++) {

					visitados.add(new Point(x, y));
					aux.clear();
					getVecinos(aux, visitados, vecinos, entrada, x, y);

					if (aux.isEmpty())
						salir = true;
					else {

						x = aux.get(0).x;
						y = aux.get(0).y;

						if (visitados.contains(new Point(x, y))) {
							if (aux.size() == 1)
								salir = true;
							else {
								aux.remove(0);

								x = aux.get(0).x;
								y = aux.get(0).y;
							}
						}
					}
				}

				if (salir) {
					salir = false;
				} else {
					float Gx = x - xBase;
					float Gy = y - yBase;
					//System.out.println("Minucia: " + xBase + " " + yBase + " tiene: " + x + " " + y);
					double radianes = Math.atan(-Gy / Gx);
					double angulo = Math.toDegrees(radianes);

					if (Gx < 0 && Gy > 0)
						angulo = angulo - 180.0;
					if (Gx < 0 && Gy <= 0)
						angulo = angulo + 180.0;

					// System.out.println("angulo: " + String.format("%.2f",angulo));
					entrada.listaMinutias.get(i).angulos = entrada.listaMinutias.get(i).angulos
							+ String.format("%.1f", angulo) + "  ";
				}
			}
		}
	}

	public static void getVecinos(ArrayList<Point> aux, ArrayList<Point> visitados, ArrayList<Point> vecinos,
			FingerPrintImage entrada, int x, int y) {
		
		// Valor Pixeles
		int p1 = entrada.getPixel(x + 1, y);
		int p2 = entrada.getPixel(x, y + 1);
		int p3 = entrada.getPixel(x - 1, y);
		int p4 = entrada.getPixel(x, y - 1);
		int p5 = entrada.getPixel(x + 1, y - 1);
		int p6 = entrada.getPixel(x - 1, y - 1);
		int p7 = entrada.getPixel(x - 1, y + 1);
		int p8 = entrada.getPixel(x + 1, y + 1);
		
		// Coordenadas
		Point c1 = new Point(x + 1, y);
		Point c2 = new Point(x, y + 1);
		Point c3 = new Point(x - 1, y);
		Point c4 = new Point(x, y - 1);
		Point c5 = new Point(x + 1, y - 1);
		Point c6 = new Point(x - 1, y - 1);
		Point c7 = new Point(x - 1, y + 1);
		Point c8 = new Point(x + 1, y + 1);

		if (p1 == 1 && !visitados.contains(c1) && !vecinos.contains(c1))
			aux.add(c1);
		if (p2 == 1 && !visitados.contains(c2) && !vecinos.contains(c2))
			aux.add(c2);
		if (p3 == 1 && !visitados.contains(c3) && !vecinos.contains(c3))
			aux.add(c3);
		if (p4 == 1 && !visitados.contains(c4) && !vecinos.contains(c4))
			aux.add(c4);
		if (p5 == 1 && !visitados.contains(c5) && !vecinos.contains(c5))
			aux.add(c5);
		if (p6 == 1 && !visitados.contains(c6) && !vecinos.contains(c6))
			aux.add(c6);
		if (p7 == 1 && !visitados.contains(c7) && !vecinos.contains(c7))
			aux.add(c7);
		if (p8 == 1 && !visitados.contains(c8) && !vecinos.contains(c8))
			aux.add(c8);
	}

	public static void mostrarAngulos(FingerPrintImage entrada) {
		Minutia m;
		for (int i = 0; i < entrada.listaMinutias.size(); i++) {
			m = entrada.listaMinutias.get(i);
			System.out.println(
					i + ") Minucia [ " + m.x + "," + m.y + " ] - tipo: " + m.tipo + " - ángulos: {" + m.angulos + "}");
		}
	}

	public static void main(String args[]) throws IOException {
		// Parte 1
		BufferedImage imagen = cargarImagen("fingerprint.png");

		FingerPrintImage imagenGrises = transformarGrises(imagen);
		calcularValores(imagenGrises);

		BufferedImage img = transformarRGB(imagenGrises, 1);

		guardarImagen(img, "imagen_en_grises.png");

		// Parte 2
		FingerPrintImage imagenEcualizada = ecualizarImagen(imagenGrises);
		calcularValores(imagenEcualizada);

		BufferedImage img2 = transformarRGB(imagenEcualizada, 1);
		guardarImagen(img2, "imagen_ecualizada.png");

		// Parte 3
		FingerPrintImage imagenBinaria = convertirBlancoNegro(imagenGrises, 128);

		BufferedImage img3 = transformarRGB(imagenBinaria, 0);
		guardarImagen(img3, "imagen_binaria.png");

		FingerPrintImage imagenFiltrada = filtrarImagen(imagenBinaria);

		BufferedImage img4 = transformarRGB(imagenFiltrada, 0);
		guardarImagen(img4, "imagen_filtrada.png");

		// Parte 4
		FingerPrintImage zansun = thinImage(imagenFiltrada);
		BufferedImage img5 = transformarRGB(zansun, 0);
		guardarImagen(img5, "imagen_fina.png");

		// Parte 5
		calcularMinutias(zansun);
		BufferedImage img6 = pintarMinutias(zansun, 0);
		guardarImagen(img6, "imagen_minutias.png");

		calcularAngulos(zansun);
		mostrarAngulos(zansun);
	}
}
