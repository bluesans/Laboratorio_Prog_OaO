# Mazmorra

Juego de mazmorra por turnos en Java.

Capas: `model` → `dao` (interfaces + implementación MySQL) → `service` → `Main`.

## Requisitos

| Herramienta | Versión usada |
|---|---|
| JDK | 17 o superior (compila con `--release 17`) |
| Maven | 3.9 o superior |
| MySQL / MariaDB | 8.x / 10.4 |

La única dependencia externa es `com.mysql:mysql-connector-j:8.3.0`, que Maven
descarga solo.

## Puesta en marcha

1. **Crear la base y el usuario** (una sola vez, como administrador de MySQL):

   ```sql
   CREATE DATABASE IF NOT EXISTS mazmorra
       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'mazmorra_app'@'localhost' IDENTIFIED BY 'CAMBIAR_ESTA_CLAVE';
   GRANT ALL PRIVILEGES ON mazmorra.* TO 'mazmorra_app'@'localhost';
   FLUSH PRIVILEGES;
   ```

   La contraseña es un ejemplo: elige una propia y usa la misma en `db.properties`.

2. **Crear las tablas**:

   ```bash
   mysql -u mazmorra_app -p mazmorra < sql/schema.sql
   ```

3. **Configurar la conexión**: copiar `db.properties.example` a `db.properties`
   y ajustar usuario y contraseña. Ese archivo está en `.gitignore`, así que
   nunca se sube al repositorio. También se puede sobrescribir con las variables
   de entorno `DB_URL`, `DB_USUARIO` y `DB_PASSWORD`, que tienen prioridad.

4. **Compilar y ejecutar**:

   ```bash
   mvn clean compile
   mvn exec:java
   ```

   En Windows, si los acentos se ven mal en la consola, ejecutar antes
   `chcp 65001` para que use UTF-8.

## Cómo se juega

El menú principal tiene tres opciones: nueva partida, reglas del juego y salir.
La segunda muestra en pantalla el objetivo, el mapa de los tres pisos, las
acciones disponibles y cómo se comporta cada criatura. Al crear la
partida se pide un nombre, se registra el aventurero (100 de vida, 20 de
ataque, 50 de oro) y las cuatro criaturas, y se entra al combate por turnos.
En cada turno se puede atacar físico, atacar con magia, huir (-10 de oro y un
golpe de despedida), tomar una poción (+30 de vida, y solo se paga lo que
realmente cura), avanzar de piso, ver el estado o abandonar. Cada criatura
derrotada paga oro (5 por nivel de amenaza).

El tipo de ataque importa: la magia ignora la resistencia del esqueleto y el
cuerpo del fantasma, pero el orco la absorbe a la mitad, así que contra él
conviene el golpe físico. Huir deja a la criatura atrás: no bloquea el piso y no
paga recompensa, pero cuesta oro y un golpe. La poción también recibe ese golpe,
porque curarse lleva un turno.

Si el golpe no la mata, la criatura responde en el acto: el orco contraataca con
su daño de ataque y el resto responde con su daño propio. El jefe final pega 10, 20 y 25 según la
fase, y solo hace el daño máximo cuando el aliento de fuego está encendido. El
aventurero empieza con 100 de vida y el daño recibido no se regenera solo, así
que atacar mal o pelear contra el fantasma con el puño desnudo puede terminar en
derrota.

La entrada se lee línea por línea con `Scanner`, así que también acepta entrada
redirigida:

```bash
mvn -q exec:java < jugada.txt
```

## Diseño

### Jerarquía de criaturas

```
Danable (interfaz)
├── Criatura (abstracta)
│   ├── Esqueleto      → resistencia física (% de reducción)
│   ├── Fantasma       → incorpóreo (el daño físico se reduce a la mitad)
│   ├── Orco           → resiste la magia y contraataca si sobrevive
│   └── DragonJefe     → cambia de fase y no permite huir
└── Aventurero         → no es una criatura: implementa Danable por su cuenta
```

`Criatura` define el esqueleto del combate (`recibirDanio`, estado, piso) y
delega en cada subclase el cálculo del daño efectivo y el daño que devuelve al
atacar (`getDanioAtaque()`), que es lo único que realmente cambia entre especies.
`Aventurero` **no** hereda de `Criatura` porque
no comparte su comportamiento (no tiene piso de mazmorra, ni amenaza, ni tipo):
solo comparte el hecho de poder recibir daño, y eso es exactamente lo que
modela la interfaz.

### Uso polimórfico de la interfaz

```java
public void resolverAtaque(Danable objetivo, int danio)
```

El servicio ataca sin conocer la clase concreta: aplica el daño a una criatura o
a un aventurero con la misma llamada, y el objeto decide cómo reaccionar
(`Fantasma` reduce el daño físico, `Aventurero` solo descuenta vida). El
`instanceof` que aparece después no elige cómo se aplica el daño, sino qué DAO
persiste el objeto actualizado, porque criaturas y aventureros viven en tablas
distintas.

### Reglas de negocio y excepciones

| Regla | Excepción |
|---|---|
| No se ataca a una criatura ya derrotada | `CriaturaYaDerrotadaException` |
| El daño debe ser positivo | `DanioInvalidoException` |
| No se baja de piso con criaturas vivas | `CriaturasPendientesException` |
| No se puede huir del jefe final | `HuidaProhibidaException` |
| No se toma poción con la vida al máximo | `VidaCompletaException` |
| La poción se paga antes de curar | `OroInsuficienteException` |
| Un aventurero derrotado no puede actuar | `AventureroDerrotadoException` |

Todas heredan de `JuegoException`, y `PersistenciaException` envuelve los errores
de JDBC para que la capa de negocio no dependa de `SQLException`.

### Persistencia

Una sola tabla `criatura` con una columna `Tipo`, la columna común `DanioAtaque` y las
columnas específicas de cada subclase en `NULL` cuando no aplican (`ResistenciaFisica`,
`EsIncorporeo`, `ResistenciaMagica`, fase y aliento del jefe). `Evadida` marca a
las criaturas que quedaron atrás al huir y `RecompensaPagada` impide cobrar dos veces la misma
recompensa. El DAO reconstruye la subclase correcta
al leer (`switch` sobre el enum `TipoCriatura`), y así el modelo queda polimórfico
aunque la base sea relacional y plana.

## Estructura del proyecto

```
src/main/java/com/mazmorra/
├── Main.java             punto de entrada
├── JuegoConsola.java     menú, combate por turnos y lectura de entrada
├── db/Conexion.java
├── model/        Danable, TipoAtaque, TipoCriatura, Criatura, Esqueleto,
│                 Fantasma, Orco, DragonJefe, Aventurero
├── dao/          CriaturaDAO, AventureroDAO
├── dao/impl/     CriaturaDAOMySQL, AventureroDAOMySQL
├── service/      JuegoService
├── service/impl/ JuegoServiceImpl
└── exception/    JuegoException, PersistenciaException y las 7 reglas
sql/schema.sql
db.properties.example
```
