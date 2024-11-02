import java.io.*;                 // Importa las clases de entrada/salida de Java, necesarias para leer y escribir datos.
import java.net.*;                // Importa las clases de red de Java, necesarias para el uso de sockets.
import java.util.List;            // Importa la interfaz List, que permite almacenar y gestionar listas de clientes.
import java.util.Map;             // Importa la interfaz Map, utilizada para almacenar pares clave-valor (nombre de usuario y color).
import java.util.concurrent.CopyOnWriteArrayList;   // Una implementación segura de List para concurrencia, adecuada para manejar múltiples clientes.
import java.util.concurrent.ConcurrentHashMap;      // Un Map seguro para concurrencia, adecuado para gestionar varios clientes simultáneamente.

public class ServidorChat {
    private static final int PORT = 12345;           // El puerto en el que el servidor escucha las conexiones entrantes.
    private static List<PrintWriter> clientes = new CopyOnWriteArrayList<>(); // Lista segura para concurrencia que contiene los flujos de salida de cada cliente.
    private static Map<String, String> userColors = new ConcurrentHashMap<>(); // Mapa seguro para concurrencia que asocia nombres de usuario con colores.

    private static final String[] COLORS = {         // Arreglo de códigos de color ANSI para colorear los mensajes de los usuarios en la consola.
            "\u001B[31m", // Rojo
            "\u001B[32m", // Verde
            "\u001B[33m", // Amarillo
            "\u001B[34m", // Azul
            "\u001B[35m", // Magenta
            "\u001B[36m"  // Cian
    };

    public static void main(String[] args) {
        System.out.println("Servidor de chat iniciado..."); // Mensaje inicial indicando que el servidor ha comenzado.

        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Crea un ServerSocket en el puerto especificado.
            while (true) {
                Socket clienteSocket = serverSocket.accept();      // Acepta conexiones entrantes de clientes.
                System.out.println("Nuevo cliente conectado: " + clienteSocket.getInetAddress()); // Imprime la dirección IP del cliente conectado.

                PrintWriter salidaCliente = new PrintWriter(clienteSocket.getOutputStream(), true); // Crea un PrintWriter para enviar mensajes al cliente.
                clientes.add(salidaCliente);                     // Añade el PrintWriter a la lista de clientes para retransmitir mensajes.

                // Inicia un nuevo hilo para manejar la conexión del cliente de forma independiente.
                new ClienteHandler(clienteSocket, salidaCliente).start();
            }
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage()); // Manejo de errores en caso de fallo en el servidor.
        }
    }

    // Clase interna para manejar cada cliente en un hilo separado.
    static class ClienteHandler extends Thread {
        private Socket socket;                     // Socket del cliente.
        private BufferedReader entrada;            // Flujo de entrada para leer mensajes del cliente.
        private PrintWriter salida;                // Flujo de salida para enviar mensajes al cliente.
        private String nombreUsuario;              // Nombre del usuario.
        private String colorUsuario;               // Color asignado al usuario.

        //metodo constructor
        public ClienteHandler(Socket socket, PrintWriter salida) {
            this.socket = socket;
            this.salida = salida;
            try {
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Inicializa el flujo de entrada para leer datos del cliente.
            } catch (IOException e) {
                System.out.println("Error al obtener el flujo de entrada: " + e.getMessage()); // Manejo de errores en caso de fallo de entrada.
            }
        }

        public void run() {
            try {
                // Solicita el nombre de usuario al cliente y asigna un color al azar.
                salida.println("Ingresa tu nombre de usuario:");
                nombreUsuario = entrada.readLine();
                colorUsuario = COLORS[(int) (Math.random() * COLORS.length)];
                userColors.put(nombreUsuario, colorUsuario); // Almacena el nombre y el color en el mapa de colores de usuario.

                // Mensaje de bienvenida al unirse al chat
                String joinMessage = colorUsuario + nombreUsuario + " se ha unido al chat." + "\u001B[0m";
                System.out.println(joinMessage);
                enviarATodos(joinMessage); // Enviar el mensaje de unión a todos los clientes.

                String mensaje;
                while ((mensaje = entrada.readLine()) != null) { // Lee los mensajes del cliente en un bucle.
                    // Detectar si el cliente ha salido del chat.
                    if (mensaje.equalsIgnoreCase(nombreUsuario + " ha salido del chat.")) {
                        enviarATodos(colorUsuario + nombreUsuario + " ha abandonado el chat." + "\u001B[0m"); // Notificar a todos los clientes.
                        System.out.println(nombreUsuario + " ha abandonado el chat."); // Imprimir en la consola del servidor.
                        break; // Salir del bucle para cerrar la conexión.
                    }
                    // Enviar el mensaje con el nombre y color a todos los clientes.
                    enviarATodos(colorUsuario + nombreUsuario + ": " + mensaje + "\u001B[0m");
                }
            } catch (IOException e) {
                System.out.println("Cliente " + nombreUsuario + " desconectado abruptamente."); // Manejo de desconexiones abruptas.
            } finally {
                // Eliminar el cliente de la lista y notificar a los demás
                clientes.remove(salida);            // Elimina el flujo de salida del cliente de la lista.
                userColors.remove(nombreUsuario);   // Elimina el nombre y color del usuario del mapa.
                enviarATodos(nombreUsuario + " ha abandonado el chat."); // Notifica a los demás que el cliente se ha desconectado.

                // Cierra el socket del cliente y libera recursos.
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el socket: " + e.getMessage()); // Manejo de errores al cerrar el socket.
                }
            }
        }

        // Método para enviar un mensaje a todos los clientes conectados.
        private void enviarATodos(String mensaje) {
            for (PrintWriter salida : clientes) {   // Recorre todos los flujos de salida en la lista de clientes.
                salida.println(mensaje);            // Envía el mensaje a cada cliente.
            }
        }
    }
}
