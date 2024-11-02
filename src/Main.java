public class Main {

    public static void main(String[] args) {
        Thread servidorThread = new Thread(() -> {
            ServidorChat.main(new String[0]); // Inicia el servidor en un hilo separado
        });
        servidorThread.start();

        try {
            Thread.sleep(2000); // Espera un par de segundos para que el servidor inicie
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ClienteChat.main(new String[0]); // Inicia el cliente en el mismo programa
    }
}
