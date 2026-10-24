import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class SistemaParqueadero {

    // SECCION CONTANTWS
    static final int CAPACIDAD_TOTAL = 10;
    static final int TARIFA_HORA_CARRO = 4000;
    static final int TARIFA_DIA_CARRO = 30000;
    static final int TARIFA_HORA_MOTO = 2000;
    static final int TARIFA_DIA_MOTO = 15000;
    static final int UMBRAL_HORAS_DIA = 8;
    static final int HORA_APERTURA = 8;
    static final int HORA_CIERRE = 22;

    static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    static final Scanner SCANNER = new Scanner(System.in);

   
    static String[] tiposVehiculo = new String[CAPACIDAD_TOTAL];
    static LocalDateTime[] fechasIngreso = new LocalDateTime[CAPACIDAD_TOTAL];

   
    static int recaudoCarros = 0;
    static int recaudoMotos = 0;

    public static void main(String[] args) {
        int opcion;
        do {
            imprimir("\n=======================================");
            imprimir("    SISTEMA DE CONTROL DE PARQUEADERO   ");
            imprimir("=======================================");
            imprimir("1. Registrar entrada de vehículo");
            imprimir("2. Registrar salida y liquidar");
            imprimir("3. Reporte de ocupación");
            imprimir("4. Reporte financiero");
            imprimir("5. Salir");
            imprimir("=======================================");
            
            opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1:
                    registroEntrada();
                    break;
                case 2:
                    registroSalida();
                    break;
                case 3:
                    reporteOcupacion();
                    break;
                case 4:
                    reporteFinanciero();
                    break;
                case 5:
                    imprimir("Saliendo del sistema. ¡Hasta luego!");
                    break;
                default:
                    imprimir("Opción inválida. Intente de nuevo.");
            }
        } while (opcion != 5);

       
        SCANNER.close();
    }
       //SECCION METODOS
    static void imprimir(String mensaje) {
        System.out.println(mensaje);
    }

    static int leerEntero(String mensaje) {
        while (true) {
            imprimir(mensaje);
            String texto = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                imprimir("Dato inválido. Debe ingresar un número entero.");
            }
        }
    }

    
    static int celdaDisponible() {
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            if (tiposVehiculo[i] == null) {
                return i;
            }
        }
        return -1;
    }

    
    static void registroEntrada() {
        int pos = celdaDisponible();
        if (pos == -1) {
            imprimir("¡El parqueadero está lleno! No hay celdas disponibles.");
            return;
        }

        imprimir("\n--- REGISTRO DE ENTRADA ---");
        imprimir("Seleccione el tipo de vehículo:");
        imprimir("1. Carro");
        imprimir("2. Moto");
        int tipoOp = leerEntero("Ingrese opción (1 o 2): ");

        String tipo = "";
        if (tipoOp == 1) {
            tipo = "Carro";
        } else if (tipoOp == 2) {
            tipo = "Moto";
        } else {
            imprimir("Tipo de vehículo inválido. Operación cancelada.");
            return;
        }

        LocalDateTime ingreso = leerFechaHora("Ingrese fecha y hora de ingreso (yyyy-MM-dd HH:mm): ");

        tiposVehiculo[pos] = tipo;
        fechasIngreso[pos] = ingreso;
        
        imprimir("¡Vehículo registrado exitosamente en la celda número " + (pos + 1) + "!");
    }

        static void registroSalida() {
        int celda = leerEntero("Ingrese el número de la celda a liberar (1 al " + CAPACIDAD_TOTAL + "): ");
        int indice = celda - 1;

        if (indice < 0 || indice >= CAPACIDAD_TOTAL) {
            imprimir("Número de celda fuera de rango.");
            return;
        }

        if (tiposVehiculo[indice] == null) {
            imprimir("La celda seleccionada ya se encuentra libre.");
            return;
        }

        LocalDateTime ingreso = fechasIngreso[indice];
        LocalDateTime salida = leerFechaHora("Ingrese fecha y hora de salida (yyyy-MM-dd HH:mm): ");

        if (salida.isBefore(ingreso)) {
            imprimir("Error: La fecha y hora de salida no puede ser anterior a la de ingreso.");
            return;
        }

        liquidacion(indice, ingreso, salida);
    }

    // Liquidación
    static void liquidacion(int indice, LocalDateTime ingreso, LocalDateTime salida) {
        String tipo = tiposVehiculo[indice];
        int celdaNum = indice + 1;

        // Cálculo de tiempo 
        long diasACobrar = contarDiasCompletos(ingreso, salida);
        LocalDateTime cursor = ingreso.plusDays(diasACobrar);
        long horasACobrar = calcularHorasRestantes(cursor, salida);

        
        if (horasACobrar >= UMBRAL_HORAS_DIA) {
            diasACobrar++;
            horasACobrar = 0;
        }

        int tarifaDia = tipo.equals("Carro") ? TARIFA_DIA_CARRO : TARIFA_DIA_MOTO;
        int tarifaHora = tipo.equals("Carro") ? TARIFA_HORA_CARRO : TARIFA_HORA_MOTO;

        int subtotalDias = (int) (diasACobrar * tarifaDia);
        int subtotalHoras = (int) (horasACobrar * tarifaHora);
        int total = subtotalDias + subtotalHoras;

        // Acumulación del recaudo
        if (tipo.equals("Carro")) {
            recaudoCarros += total;
        } else {
            recaudoMotos += total;
        }

        // Liberación de la celda
        tiposVehiculo[indice] = null;
        fechasIngreso[indice] = null;

        // Impresión de la factura detallada
        imprimir("\n=======================================");
        imprimir("          FACTURA DE SALIDA            ");
        imprimir("=======================================");
        imprimir("Celda liberada:     " + celdaNum);
        imprimir("Tipo de vehículo:   " + tipo);
        imprimir("Fecha/Hora Ingreso: " + ingreso.format(FORMATO_FECHA_HORA));
        imprimir("Fecha/Hora Salida:  " + salida.format(FORMATO_FECHA_HORA));
        imprimir("--- Desglose de cobro ---");
        imprimir("Días (" + diasACobrar + " x $" + tarifaDia + "): $" + subtotalDias);
        imprimir("Horas (" + horasACobrar + " x $" + tarifaHora + "): $" + subtotalHoras);
        imprimir("---------------------------------------");
        imprimir("TOTAL A PAGAR:      $" + total);
        imprimir("=======================================");
    }

    // Validación de formato, rangos y existencia en calendario de fecha y hora
    static LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            imprimir(mensaje);
            String texto = SCANNER.nextLine().trim();
            if (!formatoFechaHoraValido(texto)) {
                imprimir("Fecha u hora inválidas. Use yyyy-MM-dd HH:mm y el horario de atención 08:00-22:00.");
                continue;
            }

            try {
                LocalDateTime fecha = LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
                // Validación estricta de existencia en calendario (ej. evitar 30 de febrero)
                if (!fecha.format(FORMATO_FECHA_HORA).equals(texto)) {
                    imprimir("La fecha ingresada no existe en el calendario.");
                    continue;
                }
                return fecha;
            } catch (DateTimeParseException e) {
                imprimir("La fecha no existe en el calendario. Intente de nuevo.");
            }
        }
    }

    static boolean formatoFechaHoraValido(String texto) {
        if (texto.length() != 16
                || texto.charAt(4) != '-'
                || texto.charAt(7) != '-'
                || texto.charAt(10) != ' '
                || texto.charAt(13) != ':') {
            return false;
        }

        int anio = leerDigitos(texto, 0, 4);
        int mes = leerDigitos(texto, 5, 2);
        int dia = leerDigitos(texto, 8, 2);
        int hora = leerDigitos(texto, 11, 2);
        int minuto = leerDigitos(texto, 14, 2);

        return anio >= 1 && mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31
                && hora >= HORA_APERTURA && hora <= HORA_CIERRE
                && (hora != HORA_CIERRE || minuto <= 0)
                && minuto >= 0 && minuto <= 59;
    }

    static int leerDigitos(String texto, int inicio, int cantidad) {
        int valor = 0;
        for (int i = inicio; i < inicio + cantidad; i++) {
            char caracter = texto.charAt(i);
            if (caracter < '0' || caracter > '9') {
                return -1;
            }
            valor = valor * 10 + caracter - '0';
        }
        return valor;
    }

    static long contarDiasCompletos(LocalDateTime ingreso, LocalDateTime salida) {
        return ChronoUnit.DAYS.between(ingreso, salida);
    }

    static long calcularHorasRestantes(LocalDateTime cursor, LocalDateTime salida) {
        long minutosRestantes = Duration.between(cursor, salida).toMinutes();
        return (minutosRestantes + 59) / 60;
    }

    // Reporte de Ocupación: Muestra celdas ocupadas y libres
    static void reporteOcupacion() {
        int ocupadas = 0;
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            if (tiposVehiculo[i] != null) {
                ocupadas++;
            }
        }
        int libres = CAPACIDAD_TOTAL - ocupadas;

        imprimir("\n======= REPORTE DE OCUPACIÓN =======");
        imprimir("Celdas ocupadas: " + ocupadas);
        imprimir("Celdas libres:   " + libres);
        imprimir("=====================================");
    }

    // Reporte Financiero: Muestra recaudo por tipo de vehículo y total general
    static void reporteFinanciero() {
        imprimir("\n======= REPORTE FINANCIERO =======");
        imprimir("Recaudo por carros: $" + recaudoCarros);
        imprimir("Recaudo por motos:  $" + recaudoMotos);
        imprimir("Recaudo total:      $" + (recaudoCarros + recaudoMotos));
        imprimir("=================================");
    }
}   