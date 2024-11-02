import java.io.*;   // Importa las clases necesarias para la entrada y salida de datos (BufferedReader, InputStreamReader, PrintWriter).
import java.net.*;  // Importa las clases de red necesarias para la comunicación por sockets (Socket).

public class ClienteChat {
    public static void main(String[] args) {
        // Usamos try-with-resources para asegurar el cierre automático del recurso entradaUsuario.
        try (BufferedReader entradaUsuario = new BufferedReader(new InputStreamReader(System.in))) {

            // Solicitar la IP del servidor al usuario; si no ingresa nada, se usará "localhost" como valor predeterminado.
            System.out.print("Ingresa la IP del servidor (localhost por defecto): ");
            String serverAddress = entradaUsuario.readLine().trim();
            if (serverAddress.isEmpty()) serverAddress = "localhost"; // Valor predeterminado si no se ingresa nada.

            // Solicitar el puerto del servidor; si no se ingresa nada, se usará 12345 como valor predeterminado.
            System.out.print("Ingresa el puerto del servidor (12345 por defecto): ");
            String portInput = entradaUsuario.readLine().trim();
            int port = portInput.isEmpty() ? 12345 : Integer.parseInt(portInput); // Convierte el puerto a número.

            // Solicitar el nombre de usuario que se mostrará en el chat.
            System.out.print("Ingresa tu nombre de usuario: ");
            String nombreUsuario = entradaUsuario.readLine().trim();

            // Establecer conexión con el servidor utilizando el Socket.
            try (Socket socket = new Socket(serverAddress, port); // Conecta al servidor en la IP y puerto especificados.
                 BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Flujo para leer datos del servidor.
                 PrintWriter salidaServidor = new PrintWriter(socket.getOutputStream(), true)) { // Flujo para enviar datos al servidor.

                // Enviar el nombre de usuario al servidor como saludo inicial.
                salidaServidor.println(nombreUsuario ); // Envía el nombre de usuario al servidor.
                System.out.println("Conectado al servidor de chat en " + serverAddress + ":" + port);

                // Crear un hilo para leer mensajes del servidor de manera continua, sin bloquear la entrada del usuario.
                Thread lectorServidor = new Thread(() -> {
                    try {
                        String mensaje;
                        while ((mensaje = entradaServidor.readLine()) != null) { // Lee los mensajes enviados por el servidor.
                            System.out.println(mensaje); // Imprime el mensaje recibido en la consola.
                        }
                    } catch (IOException e) {
                        System.out.println("Conexión con el servidor cerrada."); // Mensaje si la conexión se cierra inesperadamente.
                    }
                });
                lectorServidor.start(); // Inicia el hilo lector del servidor.

                // Bucle principal para enviar mensajes al servidor.
                String mensajeUsuario;
                while ((mensajeUsuario = entradaUsuario.readLine()) != null) { // Lee la entrada del usuario desde la consola.
                    if (mensajeUsuario.equalsIgnoreCase("chao")) { // Verifica si el usuario escribió "chao" para salir del chat.
                        // Pregunta de confirmación para salir.
                        System.out.print("¿Deseas salir del chat? (s/n): ");
                        String respuesta = entradaUsuario.readLine().trim().toLowerCase(); // Lee la respuesta y la convierte a minúsculas.
                        if (respuesta.equals("s")) { // Si la respuesta es "s", el usuario confirma que quiere salir.
                            salidaServidor.println(nombreUsuario + " ha salido del chat."); // Notifica al servidor que el usuario ha salido.
                            System.out.println("Has salido del chat.");
                            break; // Salir del bucle para cerrar el socket y desconectarse.
                        } else if (respuesta.equals("n")) { // Si la respuesta es "n", el usuario decide quedarse en el chat.
                            System.out.println("Continuando en el chat...");
                        } else {
                            // Si la respuesta no es ni "s" ni "n", muestra un mensaje de opción no válida.
                            System.out.println("Opción no válida. Escribe 's' para salir o 'n' para continuar.");
                        }
                    } else {
                        // Enviar un mensaje normal al servidor.
                        salidaServidor.println(" " + mensajeUsuario); // Envía el mensaje con el nombre de usuario.
                    }
                }

                // Cerrar el socket después de salir del bucle para indicar al servidor la desconexión.
                socket.close(); // Cierra el socket manualmente, lo cual desconecta del servidor.

            } catch (IOException e) {
                System.out.println("Error en la conexión con el servidor: " + e.getMessage()); // Manejo de errores en la conexión con el servidor.
            }
        } catch (IOException e) {
            System.out.println("Error al leer entrada del usuario: " + e.getMessage()); // Manejo de errores en la entrada del usuario.
        }
    }
}
