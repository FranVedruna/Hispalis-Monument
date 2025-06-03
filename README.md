# Hispalis Monument

## Explora Sevilla a tu manera

**Ciclo Formativo: Desarrollo de Aplicaciones Multiplataforma (DAM)**  
**Alumno: Francisco Manuel Martín Cabello**

# Índice

1. [Introducción.](#introducción.)  
2. [Funcionalidades y tecnologías.](#funcionalidades-y-tecnologías.)  
3. [Instalación.](#guía-de-instalación)  
4. [Guia de uso.](#guía-de-uso)  
5. [Documentación](#documentación)  
6. [Diseño de interfaz (Figma)](#interfaz)  
7. [Conclusión.](#conclusión)  
8. [Contribuciones y agradecimientos.](https://www.uam.es/uam/media/doc/1606893865951/agradecimientos.pdf)  
9. [Licencia](#licencia)  
10. [Contacto.](#contacto)

# Introducción.

## Descripción del Proyecto.

**Hispalis Monument** es una aplicación Android diseñada para facilitar a los usuarios la exploración de los monumentos de Sevilla permitiendo hacerlo de una forma más personalizada.  
Los usuarios podrán elegir el orden de visita de los monumentos o, por el contrario, dejar que la aplicación les provea de la ruta más eficiente para realizar la visita.

## Justificación y Objetivos

Hispalis Monument se creó ante la dificultad que ofrece la aplicación ‘Google Map’ a la hora de poder elegir diferentes destinos.

Su objetivo es facilitar el descubrimiento y la exploración del patrimonio histórico sevillano haciéndolo accesible a cualquier persona independientemente de su conocimiento de las tecnologías.

También cómo objetivo extra, he considerado hacer la aplicación lo más ‘estándar’ posible, facilitando poder ampliar el alcance de los monumentos a otras ciudades o crear fácilmente otras aplicaciones adaptadas a esas ciudades.

## Motivación

Los puntos que más me motivaron a la hora de hacer Hispalis Monument fueron:

- Tener un reto interesante a la hora de usar coordenadas y ubicaciones.  
- Unir dos mundos tan ‘separados’ como son la tecnología y la cultura o la historia  
- Hacer accesible la visita de monumentos, especialmente para aquellos que no los conocen de antemano, evitando tener que hacer una investigación previa de los monumentos existentes en Sevilla.

# Funcionalidades y tecnologías.

## Funcionalidades

- Rutas personalizadas.  
- Información de los monumentos.  
- Generación de descripciones de los monumentos  
- Mapas integrados.  
- Generación de ruta óptima.  
- Idiomas: español e inglés.

## Tecnologías.

| Lógica de aplicación móvil | Java |
| :---- | :---- |
| Interfaz de aplicación móvil | XML |
| API | Java, Spring Boot |
| Mapas y rutas | Google Maps API, Directions API |
| Generación de descripciones | Gemini API |
| Base de datos | MySQL |
| Control de versión | GitHub |
| Diseño de interfaz | Figma |

# Guía de instalación

1. Descarga la aplicación.

Escanea el siguiente código QR:  
![QR de descarga](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/CodigoQR.png?raw=true)
O haz clic en el siguiente [enlace](https://github.com/FranVedruna/Hispalis-Monument/releases/download/v1.0.0/app-release.apk).

2. Instalación de la aplicación.

Al ser una instalación externa a la Play Store es muy probable que aparezca el siguiente mensaje:  
![Advertencia](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Instalacion0.png?raw=true)

Es comprensible la preocupación, pero al pulsar en ‘Descargar de todos modos’ se analizará la aplicación con el siguiente resultado:  
![Instalación](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Instalacion1.png?raw=true)

Ahora pulsa en Instalar y después de unos segundos se habrá instalado Hispalis Monument en su dispositivo móvil.

# Guía de uso

Una vez haya instalado la aplicación dirígete y haz clic en la aplicación ‘Hispalis Monument’

Ahora se encuentra en la pantalla de ingreso:  
![Pantalla de ingreso](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso0.png?raw=true)

Desde aquí podrás elegir el idioma con el que prefieras interactuar con la aplicación.  
También, en caso de que aún no tengas un usuario registrado, puedes pulsar en ‘Registrate’ o ‘Sign Up’ para crearte una cuenta en Hispalis Monument.

Para ello, simplemente rellena los campos solicitados. Recuerda, la contraseña debe tener un mínimo de 8 caracteres.  
![Registro](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso1.png?raw=true)

Una vez tengas un usuario registrado podrás ingresar en la aplicación.  
![Inicio de sesión](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso2.png?raw=true)
Una vez aquí podrás hacer múltiples cosas, pues buscar los monumentos que desees desde el buscador superior, mantener pulsado los monumentos que deseas visitar o ver la información de los monumentos con un simple clic, pero recuerda, para ver la información de los monumentos antes tienes que haberlos visitado, ¡disfruta de tu viaje\!

Pongámonos en el caso de que queramos visitar Plaza de Armas, Torre Sevilla y la Catedral de Sevilla. Simplemente deberás mantener pulsado dichos monumentos para iniciar la visita:  
![Selección de monumentos](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso3.png?raw=true)
Ahora bien, es hora de una pequeña decisión, ¿quieres visitarlos en el orden en el que los seleccionaste y disfrutar dando vueltas por la ciudad o prefieres que la aplicación cree una ruta óptima para poder visitarlos realizando el menor recorrido posible?

En caso de que quieras un viaje con el menor recorrido posible simplemente haz clic en ‘Viaje Optimizado’.

Independientemente de un tipo de viaje u otro, deberás pulsar en ‘Iniciar viaje’ para comenzar con la ruta.

![Inicio de ruta](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso4.png?raw=true)
La aplicación te dará siempre una ruta que enlace tu posición con el monumento que toque visitar y te dará un aviso cuando llegues al monumento:  
![Ruta activa](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso5.png?raw=true)
Ahora, ¡vamos a por el siguiente\!  
![Siguiente punto](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso6.png?raw=true)
Y a la hora de finalizar también te informaremos con un pequeño mensaje  
![Final del viaje](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso7.png?raw=true)

Ahora que hemos visitado los monumentos podemos ver la información sobre los mismos, pero, ¿y si ya me he olvidado de cuáles son los monumentos que he visitado? No te preocupes, también lo hemos considerado, por ello puedes ver todos los monumentos que hayas visitado desde tu perfíl:  
![Perfil](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Uso8.png?raw=true) 
Simplemente tendrás que hacer clic en el monumento del que quieras saber algo más.

Tenemos una pequeña recompensa cuando visites un número de monumentos, ¡suerte con ello\!

# Documentación

Usa el siguiente enlace para consultar la documentación completa del proyecto: [enlace](https://furry-floor-bf1.notion.site/Hispalis-Monument-2027a557f9d880c7bc65de04afe24300).

# Interfaz

![Interfaz principal](https://github.com/FranVedruna/Hispalis-Monument/blob/main/images/Interfaz.png?raw=true)  
Puedes consultar más detalles de la interfaz desde [este enlace](https://www.figma.com/design/nvAmQdbmYNb7RC8Cdg75fd/Hispalis-Monument?node-id=0-1&m=dev&t=3csunS3Z5dUlzBdH-1).

# Conclusión

**Hispalis Monument** ofrece una solución tecnológica eficaz para disfrutar del patrimonio de Sevilla de manera personalizada y cómoda. Combina diseño sencillo, facilidad de uso y tecnologías de ubicación para enriquecer la experiencia cultural de los usuarios. 

El desarrollo de esta aplicación ha sido una experiencia tanto estresante como enriquecedora, que no solo me ha permitido mejorar mis habilidades técnicas, sino también aprender a gestionar mejor el estrés y la presión durante el proceso.

No considero que sea un proyecto completamente terminado. Hay varias funcionalidades que me habría gustado implementar, pero por cuestiones de tiempo y con el objetivo de centrarme en la funcionalidad principal de la aplicación, se ha decidido posponerlas para una futura versión. Entre ellas, destaca:

* Permitir que los usuarios añadan una foto al visitar un monumento, enriqueciendo así la experiencia personal y visual del recorrido.  
* Añadir puntuaciones y comentarios a los monumentos.  
* Añadir enlaces para poder comprar entradas en los monumentos que lo necesiten.

# Contribuciones, agradecimientos y referencias.

Este proyecto no habría sido posible sin el apoyo, los recursos y la colaboración de distintas personas y entidades.

Agradecimientos especiales a:

- El profesorado del ciclo de Desarrollo de Aplicaciones Multiplataforma (DAM) del instituto Santa Joaquina de Vedruna quienes nos han acompañado y orientado durante estos años.  
- La comunidad de Google Developers, y Stackoverflow por la documentación y el soporte técnico.  
- A los usuarios que participaron en las pruebas, especialmente, a José Miguel Molinero Merino, quien me ha estado ayudando desde el inicio del proyecto.

Fuentes y referencias usadas:

- Google Maps Platform  
- Android Developers  
- Turismo de Sevilla

# Licencia

Está aplicación usa una licencia de tipo GNU AFFERO GENERAL PUBLIC LICENSE. Para más información dirígete al siguiente [enlace](https://github.com/FranVedruna/Hispalis-Monument?tab=AGPL-3.0-1-ov-file).

# Contacto

¿Tienes preguntas, sugerencias o quieres colaborar?  
 No dudes en ponerte en contacto:

**Email:** [hispalismonument@gmail.com](mailto:hispalismonument@gmail.com)
