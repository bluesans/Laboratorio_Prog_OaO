package com.mazmorra.db;

import com.mazmorra.exception.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {

    private static final String CLAVE_URL = "db.url";
    private static final String CLAVE_USUARIO = "db.usuario";
    private static final String CLAVE_PASSWORD = "db.password";

    private static final String VAR_URL = "DB_URL";
    private static final String VAR_USUARIO = "DB_USUARIO";
    private static final String VAR_PASSWORD = "DB_PASSWORD";

    private static Properties propiedades;

    private Conexion() {
        throw new UnsupportedOperationException("Clase utilitaria: no se debe instanciar.");
    }

    public static Connection getConexion() throws PersistenciaException {
        // Cada llamada abre una conexión nueva; se cierra con try-with-resources.
        try {
            return DriverManager.getConnection(getUrl(), getUsuario(), obtenerPassword());
        } catch (IllegalStateException e) {
            throw new PersistenciaException(
                    "Configuración de la base de datos inválida: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar con la base de datos.", e);
        }
    }

    public static String getUrl() {
        return obtenerValor(CLAVE_URL, VAR_URL);
    }

    public static String getUsuario() {
        return obtenerValor(CLAVE_USUARIO, VAR_USUARIO);
    }

    private static String obtenerPassword() {
        return obtenerValor(CLAVE_PASSWORD, VAR_PASSWORD);
    }

    private static String obtenerValor(String clave, String variableEntorno) {
        // Env > db.properties > classpath.
        String desdeEntorno = System.getenv(variableEntorno);
        if (desdeEntorno != null && !desdeEntorno.isBlank()) {
            return desdeEntorno;
        }
        Properties cargadas = propiedades();
        String desdePropiedades = cargadas.getProperty(clave);
        if (desdePropiedades != null && !desdePropiedades.isBlank()) {
            return desdePropiedades;
        }
        if (cargadas.isEmpty()) {
            throw new IllegalStateException("No se encontró db.properties: copia db.properties.example "
                    + "a db.properties en la carpeta del proyecto y ejecuta el juego desde ahí.");
        }
        throw new IllegalStateException("Falta " + clave + " en db.properties o en " + variableEntorno + ".");
    }

    private static synchronized Properties propiedades() {
        if (propiedades == null) {
            propiedades = cargarPropiedades();
        }
        return propiedades;
    }

    private static Path buscarArchivo() {
        Path enDirectorioActual = Path.of("db.properties");
        if (Files.isRegularFile(enDirectorioActual)) {
            return enDirectorioActual;
        }
        // Si se lanza desde otra carpeta, sube desde las clases compiladas hasta la raíz.
        try {
            Path ubicacion = Path.of(Conexion.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path carpeta = Files.isDirectory(ubicacion) ? ubicacion : ubicacion.getParent();
            for (int i = 0; i < 3 && carpeta != null; i++) {
                Path candidato = carpeta.resolve("db.properties");
                if (Files.isRegularFile(candidato)) {
                    return candidato;
                }
                carpeta = carpeta.getParent();
            }
        } catch (URISyntaxException | IllegalArgumentException | SecurityException e) {
            return null;
        }
        return null;
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        Path archivo = buscarArchivo();
        if (archivo != null) {
            try (InputStream entrada = Files.newInputStream(archivo);
                 Reader lector = new InputStreamReader(entrada, StandardCharsets.UTF_8)) {
                propiedades.load(lector);
                return propiedades;
            } catch (IOException e) {
                throw new IllegalStateException(
                        "No se pudo leer el archivo de configuracion db.properties en "
                        + archivo.toAbsolutePath(), e);
            }
        }
        try (InputStream recurso = Conexion.class.getResourceAsStream("/db.properties")) {
            if (recurso != null) {
                try (Reader lector = new InputStreamReader(recurso, StandardCharsets.UTF_8)) {
                    propiedades.load(lector);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer el recurso /db.properties del classpath.", e);
        }
        return propiedades;
    }
}
