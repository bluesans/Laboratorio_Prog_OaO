## 1. Idea, consigna y planificación

- “tengo esta idea de juego ahora necesito que lo planees y lo hagas en java respetando lo q dicen las consignas del pdf 2. preguntame cualquier cosa. estamos utilizando estructuras MVC y jdbc y mysql y DAO.”
- “el proyecto ponlo dentro de una carpeta llamada Ejercitacion 14”.
- “C:\Users\WinterOS\Downloads\MavenProyect.zip C:\Users\WinterOS\Downloads\mysql-connector-j-26.7.0.jar tengo estas dos cosas tambien”.
- Respuestas al cuestionario inicial: instalar JDK 21 + Maven, Main con demo guionada que fuerce los errores, base `mazmorra` con usuario dedicado y `git init` sin commits.

## 2. Limpieza de estilo y nombres

- “pon un poco mas de comentarios.”
- “cambia el nombre dragon jefe. por jefe dragon, queda mas natural”.

## 3. Consola interactiva

- “ok ahora quiero que no sea una demo quiero que sea interactivo y que cumpla estas condiciones:” + las cinco condiciones de la consigna (jerarquía, interfaz polimórfica, MySQL/DAO, excepciones propias y Main que fuerce los errores).
- “ok pon una linea blanca despues de cada interaccion asi se ve mejor. y que significa verificacion de reglas?”
- “sacalo solo pon lo interactivo. si quieres que sea algo de debug que tenga que cambiar una variable para que se pueda usar.”
- “ok haz una segunda opcion junto a nueva partida donde explica las reglas del juego y como funciona”.

## 4. Explicación y calidad

- “haz esto - Unificar fuerzaAtaque con DanioAtaque del Orco. - Atomicidad total del turno…”.
- “ahora si quiero que hagas los test avisame cuando todo este terminado”.
- “no lo subas sin mi permiso”.

## 5. Revisiones externas que pegaste

- Pegaste varias revisiones de código (textos largos) y pediste evaluarlas, decir qué era cierto y aplicar lo que correspondiera. De ahí salieron, entre otras cosas: turno enemigo, rebalanceo, `Evadida`, `ResistenciaMagica`, `RecompensaPagada`, transacciones, tests y la limpieza de setters.

## 6. Entrega de la 14 en GitHub
- “que por defecto este en false y en el readme cambie a que sea esto asi: Juego de mazmorra por turnos en Java. / Dividido en capas: model → dao (interfaces + implementación MySQL) → service → Main. asi queda mejor”.
- “la carpeta de test. es necesaria?”
- “perfecto no lo borres.”

## 7. Ejercitación 16: decisión de GUI

- “ahora tengo que hacer una gui. hice una nueva carpeta D:\labprogramacion-oao\Ejercitacion 16 quiero que lo hagamos en swift capaz pero revisa las opciones y vemos.”
- “mandale la swing y flatlaf”

## 8. El videojuego con sprites

- “necesito que sea un videojuego osea que tenga sprites y todo no una ventana y ya. que sea un video juego jugable preguntame lo que necesites antes de continuar”.
- Respuestas al cuestionario: Swing 2D, roguelike por turnos, pixel art dibujado por código, panel de datos dentro del juego, sala fija por piso.
- “ok. quedo mejor. pero revisa los cambios que hice al readme … la fotos de demostracion las hago yo asi que en la parte de capturas pon un place holder. … que nada mas se mueva el personaje con wasd y que no subas las escaleras con e ni nada asi simplemente con avanzar hacia la puerta que suba. y que al pegar nada mas se pegue con el 1 y 2 no con wasd hacia el enemigo. pule todo y avisame cuando estes.”
- “y q la magia sea solo con el 2”.
- “ok. me gustaron los cambios en la pantalla del principio abajo de mazmorra saca el subtitulo que dice: "un aventurero etc".
- “muestra el precio dinamicamente dependiendo de cuanto te va a curar y cuanto va a costar. osea quw diga exactqmente lo q te saca”.
- “ok. bien mejora el jefe dragon para que cuando use su aliento de fuego tire verdadero fuego. y mejora la animacion de muerte de todos los enemigos.”
- “cuando te curas quiero que nada mas te pegue un enemigo cuando esten al lado tuyo. y quiero que nunca se pongan los dos al mismo tiempo al lado tuyo. que sea random quien llega primero pero que uno siempre se quede atras.”
- “que queden mas lejos los que no son el "guardian" ya que quede en diagonal al jugador y con un movimiento ya estan en el otro. quiero que el "guardian" le corte el camino y tenga que pelear con el si o si.”
- “ok creo que sigue el problema de que pega cuando cambias de guardian sigue y subele la distancia porfa.”
- “D:\labprogramacion-oao\paneljuego.java que es eso?”

## 9. Correcciones de la cátedra y validación

- “preguntame todo antes de cambiar algo ” + el documento `Mazmorra correcciones para las Ejercitaciones 14 y 16.md`.
- Respuestas a mis preguntas: arreglar los tres bugs + tests; modelo valida forma y Service negocio; **no** dividir `PanelJuego`; **no** `SwingWorker`; **sí** inyectar DAOs desde `Main`; **sí** sincronizar con la 14.
- “corrige pero sin la captura”.

## 10. README, capturas y verificación

- “ok revisa los cambios que hice a los readme y las capturas q hice”.
- “si porfa” (para corregir los enlaces de las capturas y los detalles de los README).
- “como hago la foto de la error excepcion?”
- “listo ahi puse la foto y en el readme lo correspondiente”.
- “ok ahora verifica todo extensivamente. absolutamente todo de los dos proyectos para ver si hay cualquier error.”
- “sube a supermemory todo”.
- “ok verifica una ultima vez para estar seguros.”
