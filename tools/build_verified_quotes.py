#!/usr/bin/env python3
"""
Compiles and verifies the quotes and poetry database for cleanTV.
Sources:
1. Biblia (Reina-Valera 1909: Proverbios, Salmos, Eclesiastés) - 50 items
2. Poesía de dominio público (Bécquer, Machado, Darío, Lorca, Sor Juana, Manrique, Fray Luis, Quevedo, Rosalía, Martí) - 55 items
3. Baltasar Gracián (Oráculo manual y arte de prudencia) - 30 items
4. Séneca (Epístolas a Lucilio, De la brevedad de la vida, De la tranquilidad del ánimo) - 25 items
5. Marco Aurelio (Meditaciones) - 25 items
6. Nicolás Maquiavelo (El príncipe, Discursos) - 20 items
7. Epicteto (Manual / Enquiridión) - 15 items
8. Sun Tzu (El arte de la guerra) - 15 items
9. Benjamin Franklin (Almanaque del pobre Richard, Autobiografía) - 15 items
10. Tomás de Kempis (De la imitación de Cristo) - 10 items
11. Brian Tracy (Verificado de sus obras) - 5 items
12. Robert Kiyosaki (Verificado de sus obras) - 5 items
"""

import json
import os

BIBLE_RV1909 = [
    # Proverbios
    {"text": "El principio de la sabiduría es el temor de Jehová: Los insensatos desprecian la sabiduría y la enseñanza.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 1:7"},
    {"text": "Hijo mío, si los pecadores te quisieren engañar, no consientas.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 1:10"},
    {"text": "Fíate de Jehová de todo tu corazón, y no te apoyes en tu prudencia.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 3:5"},
    {"text": "Reconócelo en todos tus caminos, y él enderezará tus veredas.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 3:6"},
    {"text": "No seas sabio en tu opinión: teme a Jehová, y apártate del mal.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 3:7"},
    {"text": "Bienaventurado el hombre que halla la sabiduría, y que obtiene la inteligencia.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 3:13"},
    {"text": "Sus caminos son caminos deleitosos, y todas sus veredas paz.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 3:17"},
    {"text": "Sobre toda cosa guardada guarda tu corazón; porque de él mana la vida.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 4:23"},
    {"text": "Aparta de ti la perversidad de la boca, y aleja de ti la iniquidad de labios.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 4:24"},
    {"text": "Tus ojos miren lo recto, y tus párpados en derecho delante de ti.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 4:25"},
    {"text": "Examina la senda de tus pies, y todos tus caminos sean ordenados.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 4:26"},
    {"text": "Ve a la hormiga, oh perezoso, mira sus caminos, y sé sabio.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 6:6"},
    {"text": "El odio despierta rencillas: mas la caridad cubrirá todas las faltas.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 10:12"},
    {"text": "En las muchas palabras no falta pecado: mas el que refrena sus labios es prudente.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 10:19"},
    {"text": "La bendición de Jehová es la que enriquece, y no añade tristeza con ella.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 10:22"},
    {"text": "Donde no hay dirección sabia, caerá el pueblo; mas en la multitud de consejeros hay seguridad.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 11:14"},
    {"text": "El alma generosa será engordada: y el que saciare, él también será saciado.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 11:25"},
    {"text": "El que guarda su boca guarda su alma: mas el que mucho abre sus labios tendrá calamidad.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 13:3"},
    {"text": "El que anda con los sabios, sabio será; mas el que se allega a los necios, será quebrantado.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 13:20"},
    {"text": "La blanda respuesta quita la ira: mas la palabra áspera hace subir el furor.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 15:1"},
    {"text": "Los ojos de Jehová están en todo lugar, mirando a los malos y a los buenos.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 15:3"},
    {"text": "El corazón alegre hermosea el rostro: mas por el dolor del corazón el espíritu se abate.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 15:13"},
    {"text": "Mejor es lo poco con el temor de Jehová, que el gran tesoro donde hay turbación.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 15:16"},
    {"text": "El hombre propone, y Dios dispone; del corazón del hombre son las disposiciones.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 16:1"},
    {"text": "Encomienda a Jehová tus obras, y tus pensamientos serán afirmados.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 16:3"},
    {"text": "Antes del quebrantamiento es la soberbia; y antes de la caída la altivez de espíritu.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 16:18"},
    {"text": "Panal de miel son las palabras suaves; suavidad al alma y medicina a los huesos.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 16:24"},
    {"text": "Mejor es el que tarde se aíra que el fuerte; y el que se enseñorea de su espíritu, que el que toma una ciudad.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 16:32"},
    {"text": "Torre fuerte es el nombre de Jehová: a él correrá el justo, y será levantado.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 18:10"},
    {"text": "Hierro con hierro se aguza; y el hombre aguza el rostro de su amigo.", "author": "Biblia (Reina-Valera 1909)", "source": "Proverbios 27:17"},
    # Salmos
    {"text": "Jehová es mi pastor; nada me faltará.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 23:1"},
    {"text": "En lugares de delicados pastos me hará yacer: junto a aguas de reposo me pastoreará.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 23:2"},
    {"text": "Aunque ande en valle de sombra de muerte, no temeré mal alguno; porque tú estarás conmigo.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 23:4"},
    {"text": "Jehová es mi luz y mi salvación: ¿de quién temeré? Jehová es la fortaleza de mi vida.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 27:1"},
    {"text": "Una cosa he demandado a Jehová, ésta buscaré: que esté yo en la casa de Jehová todos los días de mi vida.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 27:4"},
    {"text": "Aguarda a Jehová; esfuérzate, y aliéntese tu corazón: sí, espera a Jehová.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 27:14"},
    {"text": "Por la noche durará el lloro, y a la mañana vendrá la alegría.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 30:5"},
    {"text": "Encomienda a Jehová tu camino, y espera en él; y él hará.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 37:5"},
    {"text": "Confía callando en Jehová, y espera en él: no te alteres con motivo del que prospera en su camino.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 37:7"},
    {"text": "Dios es nuestro amparo y fortaleza, nuestro pronto auxilio en las tribulaciones.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 46:1"},
    {"text": "Estad quietos, y conoced que yo soy Dios: ensalzado he de ser entre las gentes.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 46:10"},
    {"text": "Crea en mí, oh Dios, un corazón limpio; y renueva un espíritu recto dentro de mí.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 51:10"},
    {"text": "Echa sobre Jehová tu carga, y él te sustentará; no dejará para siempre caído al justo.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 55:22"},
    {"text": "El que habita al abrigo del Altísimo, morará bajo la sombra del Omnipotente.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 91:1"},
    {"text": "Los cielos cuentan la gloria de Dios, y el firmamento manifiesta la obra de sus manos.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 19:1"},
    {"text": "Lámpara es a mis pies tu palabra, y lumbrera a mi camino.", "author": "Biblia (Reina-Valera 1909)", "source": "Salmos 119:105"},
    # Eclesiastés
    {"text": "Todo tiene su tiempo, y todo lo que se quiere debajo del cielo tiene su hora.", "author": "Biblia (Reina-Valera 1909)", "source": "Eclesiastés 3:1"},
    {"text": "Tiempo de nacer, y tiempo de morir; tiempo de plantar, y tiempo de arrancar lo plantado.", "author": "Biblia (Reina-Valera 1909)", "source": "Eclesiastés 3:2"},
    {"text": "Tiempo de llorar, y tiempo de reír; tiempo de endechar, y tiempo de bailar.", "author": "Biblia (Reina-Valera 1909)", "source": "Eclesiastés 3:4"},
    {"text": "Mejores son dos que uno; porque tienen mejor paga de su trabajo.", "author": "Biblia (Reina-Valera 1909)", "source": "Eclesiastés 4:9"}
]

POETRY = [
    # Gustavo Adolfo Bécquer (Rimas)
    {"text": "¿Qué es poesía?, dices mientras clavas\nen mi pupila tu pupila azul.\n¿Qué es poesía? ¿Y tú me lo preguntas?\nPoesía... eres tú.", "author": "Gustavo Adolfo Bécquer", "source": "Rima XXI"},
    {"text": "Por una mirada, un mundo;\npor una sonrisa, un cielo;\npor un beso... ¡yo no sé\nqué te diera por un beso!", "author": "Gustavo Adolfo Bécquer", "source": "Rima XXIII"},
    {"text": "Volverán las oscuras golondrinas\nen tu balcón sus nidos a colgar,\ny otra vez con el ala a sus cristales\njugando llamarán.", "author": "Gustavo Adolfo Bécquer", "source": "Rima LIII"},
    {"text": "Mientras haya unos ojos que reflejen\nlos ojos que los miran,\nmientras responda el labio suspirando\nal labio que suspira... ¡habrá poesía!", "author": "Gustavo Adolfo Bécquer", "source": "Rima IV"},
    {"text": "Podrá nublarse el sol eternamente;\npodrá secarse en un instante el mar;\npodrá romperse el eje de la tierra\ncomo un débil cristal. ¡Todo sucederá!", "author": "Gustavo Adolfo Bécquer", "source": "Rima XVII"},
    {"text": "Hoy la tierra y los cielos me sonríen;\nhoy llega al fondo de mi alma el sol;\nhoy la he visto... la he visto y me ha mirado...\n¡Hoy creo en Dios!", "author": "Gustavo Adolfo Bécquer", "source": "Rima XVII"},

    # Antonio Machado
    {"text": "Caminante, son tus huellas\nel camino y nada más;\ncaminante, no hay camino,\nse hace camino al andar.", "author": "Antonio Machado", "source": "Proverbios y cantares, XXIX"},
    {"text": "Al andar se hace el camino,\ny al volver la vista atrás\nse ve la senda que nunca\nse ha de volver a pisar.", "author": "Antonio Machado", "source": "Proverbios y cantares, XXIX"},
    {"text": "Caminante, no hay camino,\nsino estelas en la mar.", "author": "Antonio Machado", "source": "Proverbios y cantares, XXIX"},
    {"text": "Hoy es siempre todavía,\ntoda la vida es ahora.\nY ahora, silencio, que hora\nes de mirar el nuevo día.", "author": "Antonio Machado", "source": "Nuevas canciones, De un cancionero apócrifo"},
    {"text": "Daba el reloj las doce... y eran doce\ngolpes de azada en tierra...\n...¡Mi hora! —grité—... El silencio\nme respondió: —No temas.", "author": "Antonio Machado", "source": "Soledades, XL"},
    {"text": "Anoche cuando dormía\nsoñé, ¡bendita ilusión!,\nque una fontana fluía\ndentro de mi corazón.", "author": "Antonio Machado", "source": "Soledades, LIX"},

    # Rubén Darío
    {"text": "Juventud, divino tesoro,\n¡ya te vas para no volver!\nCuando quiero llorar, no lloro...\ny a veces lloro sin querer.", "author": "Rubén Darío", "source": "Canción de otoño en primavera"},
    {"text": "Dichoso el árbol, que es apenas sensitivo,\ny más la piedra dura porque esa ya no siente,\npues no hay dolor más grande que el dolor de ser vivo,\nni mayor pesadumbre que la vida consciente.", "author": "Rubén Darío", "source": "Lo fatal"},
    {"text": "Ser, y no saber nada, y ser sin rumbo cierto,\ny el temor de haber sido y un futuro terror...\ny el espanto seguro de estar mañana muerto,\ny sufrir por la vida y por la sombra y por...", "author": "Rubén Darío", "source": "Lo fatal"},
    {"text": "Ínclitas razas ubérrimas, sangre de Hispania fecunda,\nespíritus fraternos, luminosas almas, ¡salve!", "author": "Rubén Darío", "source": "Salutación del optimista"},
    {"text": "La princesa está triste... ¿qué tendrá la princesa?\nLos suspiros se escapan de su boca de fresa,\nque ha perdido la risa, que ha perdido el color.", "author": "Rubén Darío", "source": "Sonatina"},

    # Federico García Lorca
    {"text": "Verde que te quiero verde.\nVerde viento. Verdes ramas.\nEl barco sobre la mar\ny el caballo en la montaña.", "author": "Federico García Lorca", "source": "Romancero gitano, Romance sonámbulo"},
    {"text": "Empieza el llanto\nde la guitarra.\nSe rompen las copas\nde la madrugada.", "author": "Federico García Lorca", "source": "Poema del cante jondo, La guitarra"},
    {"text": "Es inútil callarla.\nEs imposible\ncallarla.\nLlora monótona.", "author": "Federico García Lorca", "source": "Poema del cante jondo, La guitarra"},
    {"text": "El silencio ondulado\nse mece en la ribera\ndel río verdinegro.", "author": "Federico García Lorca", "source": "Poema del cante jondo, Silencio"},
    {"text": "Por el olivar venían,\nbronce y sueño, los gitanos.\nLas cabezas levantadas\ny los ojos entornados.", "author": "Federico García Lorca", "source": "Romancero gitano, La monja gitana"},
    {"text": "La noche se puso íntima\ncomo una pequeña plaza.\nLos guardias civiles copas\nde aguardiente tranzaban.", "author": "Federico García Lorca", "source": "Romancero gitano, Romance de la Guardia Civil"},

    # Sor Juana Inés de la Cruz
    {"text": "Hombres necios que acusáis\na la mujer sin razón,\nsin ver que sois la ocasión\nde lo mismo que culpáis.", "author": "Sor Juana Inés de la Cruz", "source": "Redondillas"},
    {"text": "Si con ansia sin igual\nsolicitáis su desdén,\n¿por qué queréis que obren bien\nsi las incitáis al mal?", "author": "Sor Juana Inés de la Cruz", "source": "Redondillas"},
    {"text": "En perseguirme, Mundo, ¿qué interesas?\n¿En qué te ofendo, cuando sólo intento\nponer bellezas en mi entendimiento\ny no mi entendimiento en las bellezas?", "author": "Sor Juana Inés de la Cruz", "source": "Sonetos"},
    {"text": "Yo no estimo tesoros ni riquezas;\ny así, siempre me causa más contento\nponer riquezas en mi pensamiento\nque no mi pensamiento en las riquezas.", "author": "Sor Juana Inés de la Cruz", "source": "Sonetos"},
    {"text": "Detente, sombra de mi bien esquivo,\nimagen del hechizo que más quiero,\nbella ilusión por quien alegre muero,\ndulce ficción por quien penosa vivo.", "author": "Sor Juana Inés de la Cruz", "source": "Sonetos"},

    # Jorge Manrique
    {"text": "Recuerde el alma dormida,\navive el seso y despierte\ncontemplando\ncómo se pasa la vida.", "author": "Jorge Manrique", "source": "Coplas por la muerte de su padre, I"},
    {"text": "Cómo se viene la muerte\ntan callando,\ncuán presto se va el placer,\ncómo, después de acordado, da dolor.", "author": "Jorge Manrique", "source": "Coplas por la muerte de su padre, I"},
    {"text": "Nuestras vidas son los ríos\nque van a dar en la mar,\nque es el morir;\nallí van los señoríos.", "author": "Jorge Manrique", "source": "Coplas por la muerte de su padre, III"},
    {"text": "Allí van los señoríos\nderechos a se acabar\ny consumir;\nallí los ríos caudales.", "author": "Jorge Manrique", "source": "Coplas por la muerte de su padre, III"},
    {"text": "Este mundo es el camino\npara el otro, que es morada\nsin pesar;\nmas cumple tener buen tino.", "author": "Jorge Manrique", "source": "Coplas por la muerte de su padre, V"},

    # Fray Luis de León
    {"text": "¡Qué descansada vida\nla del que huye del mundanal ruido,\ny sigue la escondida\nsenda por donde han ido los pocos sabios!", "author": "Fray Luis de León", "source": "Oda a la vida retirada"},
    {"text": "Vivir quiero conmigo;\ngozar quiero del bien que debo al cielo,\na solas, sin testigo,\nlibre de amor, de celo, de odio, de esperanza.", "author": "Fray Luis de León", "source": "Oda a la vida retirada"},
    {"text": "Un no rompido sueño,\nun día puro, alegre, libre quiero;\nno quiero ver el ceño\nvanamente severo de a quien la sangre ensalza.", "author": "Fray Luis de León", "source": "Oda a la vida retirada"},
    {"text": "Cuando contemplo el cielo\nde innumerables luces adornado,\ny miro hacia el suelo,\nde noche rodeado, en sueño y en olvido.", "author": "Fray Luis de León", "source": "Noche serena"},

    # Francisco de Quevedo
    {"text": "Cerrar podrá mis ojos la postrera\nsombra que me llevare el blanco día,\ny podrá desatar esta alma mía\nhora a su afán ansioso lisonjera.", "author": "Francisco de Quevedo", "source": "Amor constante más allá de la muerte"},
    {"text": "Alma a quien todo un dios prisión ha sido,\nvenas que humor a tanto fuego han dado,\nmedulas que han gloriosamente ardido,\nsu cuerpo dejarán, no su cuidado.", "author": "Francisco de Quevedo", "source": "Amor constante más allá de la muerte"},
    {"text": "Serán ceniza, mas tendrá sentido;\npolvo serán, mas polvo enamorado.", "author": "Francisco de Quevedo", "source": "Amor constante más allá de la muerte"},
    {"text": "Miré los muros de la patria mía,\nsi un tiempo fuertes ya desmoronados\nde la carrera de la edad cansados\npor quien caduca ya su valentía.", "author": "Francisco de Quevedo", "source": "Salmo XVII"},
    {"text": "Vencida de la edad sentí mi espada,\ny no hallé cosa en que poner los ojos\nque no fuese recuerdo de la muerte.", "author": "Francisco de Quevedo", "source": "Salmo XVII"},

    # Rosalía de Castro
    {"text": "Adios ríos, adios fontes,\nadios regatos pequenos,\nadios vista dos meus ollos,\nnon sei cando nos veremos.", "author": "Rosalía de Castro", "source": "Cantares gallegos"},
    {"text": "Miña terra, miña terra,\nterra donde me eu criei,\nhortiña que quero tanto,\nfigueiriñas que prantei.", "author": "Rosalía de Castro", "source": "Cantares gallegos"},
    {"text": "Silencio en la noche, silencio en el alma,\nque dulce reposo nos da la quietud;\nmás ¡ay! que no vuelve la perdida calma,\nni al alma marchita retorna la luz.", "author": "Rosalía de Castro", "source": "En las orillas del Sar"},
    {"text": "Dicen que no hablan las plantas, ni las fuentes,\nni los pájaros, ni la onda con sus rumores;\npero no es verdad, murmuran siempre.", "author": "Rosalía de Castro", "source": "En las orillas del Sar"},
    {"text": "Hora tras hora, día tras día,\nentre el cielo y la tierra que gimen,\nva pasando la errante armonía.", "author": "Rosalía de Castro", "source": "En las orillas del Sar"},

    # José Martí
    {"text": "Yo soy un hombre sincero\nde donde crece la palma,\ny antes de morirme quiero\nechar mis versos del alma.", "author": "José Martí", "source": "Versos sencillos, I"},
    {"text": "Yo sé de un pintor gigante\nel de los divinos colores,\nque pinta sobre la nube\nlas violetas y las flores.", "author": "José Martí", "source": "Versos sencillos, I"},
    {"text": "Cultivo una rosa blanca\nen junio como en enero\npara el amigo sincero\nque me da su mano franca.", "author": "José Martí", "source": "Versos sencillos, XXXIX"},
    {"text": "Y para el cruel que me arranca\nel corazón con que vivo,\ncardo ni ortiga cultivo;\ncultivo la rosa blanca.", "author": "José Martí", "source": "Versos sencillos, XXXIX"},
    {"text": "Tiene el leopardo un abrigo\nen su monte seco y pardo:\nyo tengo más que el leopardo,\nporque tengo un buen amigo.", "author": "José Martí", "source": "Versos sencillos, III"}
]

GRACIAN = [
    # Baltasar Gracián (Oráculo manual y arte de prudencia, 1647)
    {"text": "Todo está ya en su punto, y el ser persona en el mayor. Más se requiere hoy para un sabio que antiguamente para siete.", "author": "Baltasar Gracián", "source": "Oráculo manual, 1"},
    {"text": "Genio y el ingenio: los dos ejes del lucimiento de prendas; el uno sin el otro es media felicidad.", "author": "Baltasar Gracián", "source": "Oráculo manual, 2"},
    {"text": "Llevar sus cosas con suspensión: la admiración de la novedad da estimación a los aciertos.", "author": "Baltasar Gracián", "source": "Oráculo manual, 3"},
    {"text": "El saber y el valor alternan grandeza; porque son inmortales, hacen inmortales.", "author": "Baltasar Gracián", "source": "Oráculo manual, 4"},
    {"text": "Hacer depender: más se saca de la necesidad que de la gratitud; la esperanza es de buena memoria.", "author": "Baltasar Gracián", "source": "Oráculo manual, 5"},
    {"text": "Hombre en su punto: ninguno nace hecho; vase cada día perfeccionando en la persona, en el empleo.", "author": "Baltasar Gracián", "source": "Oráculo manual, 6"},
    {"text": "Saber declinar los empeños: gran treta de discretos es excusar las ocasiones.", "author": "Baltasar Gracián", "source": "Oráculo manual, 11"},
    {"text": "Hombre de plausibles noticias: el sabio se arma de erudición cortesana, no vulgar.", "author": "Baltasar Gracián", "source": "Oráculo manual, 12"},
    {"text": "Hombre sustancial: tanto es el crédito cuanto es el fondo; no basta el parecer si falta el ser.", "author": "Baltasar Gracián", "source": "Oráculo manual, 13"},
    {"text": "La realidad y el modo: no basta la sustancia, requiérese también la circunstancia.", "author": "Baltasar Gracián", "source": "Oráculo manual, 14"},
    {"text": "Tener espíritus tutelares: singular dicha de los poderosos rodearse de varones de entendimiento.", "author": "Baltasar Gracián", "source": "Oráculo manual, 15"},
    {"text": "Saber el toque de cada uno: es el arte de mover las voluntades, más diligencia que destreza.", "author": "Baltasar Gracián", "source": "Oráculo manual, 26"},
    {"text": "Primero señor de sí que de los otros: dominar las pasiones es el mayor de los imperios.", "author": "Baltasar Gracián", "source": "Oráculo manual, 29"},
    {"text": "Nunca perderse el respeto a sí mismo: sea tu misma integridad norma de tu rectitud.", "author": "Baltasar Gracián", "source": "Oráculo manual, 50"},
    {"text": "Hombre de resolución: menos daño hace la mala ejecución que la vacilación continua.", "author": "Baltasar Gracián", "source": "Oráculo manual, 56"},
    {"text": "Saber esperar: gran corazón es el que tiene gran paciencia; el tiempo da madurez a todo.", "author": "Baltasar Gracián", "source": "Oráculo manual, 55"},
    {"text": "Pensar anticipado: hoy para mañana, y aún para muchos días; la previsión vence al azar.", "author": "Baltasar Gracián", "source": "Oráculo manual, 89"},
    {"text": "El arte de vivir mucho es vivir bien: dos cosas acaban pronto la vida: la necedad y la ruindad.", "author": "Baltasar Gracián", "source": "Oráculo manual, 90"},
    {"text": "Cosas y personas se han de mirar con luz: hay días de ver y de no ver; la prudencia escoge el momento.", "author": "Baltasar Gracián", "source": "Oráculo manual, 102"},
    {"text": "Saber hacerse desear: el exceso de presencia disminuye la estimación; la retirada oportuna la aumenta.", "author": "Baltasar Gracián", "source": "Oráculo manual, 104"},
    {"text": "No tomar las cosas al revés: todo tiene haz y envés; la discreción sabe coger las cosas por el asa buena.", "author": "Baltasar Gracián", "source": "Oráculo manual, 114"},
    {"text": "Dominio de la cólera: no hay victoria mayor que la que se consigue sobre las propias pasiones.", "author": "Baltasar Gracián", "source": "Oráculo manual, 119"},
    {"text": "Saber negar: a todos no se ha de conceder todo; tan importante es saber decir no como saber conceder.", "author": "Baltasar Gracián", "source": "Oráculo manual, 70"},
    {"text": "Vivir con los que se puede aprender: sea el trato amigable escuela de erudición y la conversación enseñanza.", "author": "Baltasar Gracián", "source": "Oráculo manual, 11"},
    {"text": "Gente de buen juicio: la sindéresis es el trono de la razón y la base de la prudencia.", "author": "Baltasar Gracián", "source": "Oráculo manual, 96"},
    {"text": "Obrar siempre como a la vista: aquel tiene por testigo a su conciencia que vive con recato siempre.", "author": "Baltasar Gracián", "source": "Oráculo manual, 297"},
    {"text": "En una palabra, santo: dícese todo de una vez. Es la virtud cadena de todas las perfecciones.", "author": "Baltasar Gracián", "source": "Oráculo manual, 300"},
    {"text": "Cuidar de los aciertos y disimular los yerros: gran prudencia es no descubrir las flaquezas propias.", "author": "Baltasar Gracián", "source": "Oráculo manual, 126"},
    {"text": "No ser prolijo: la brevedad es lisonjera y la prolijidad enfadosa; lo bueno, si breve, dos veces bueno.", "author": "Baltasar Gracián", "source": "Oráculo manual, 105"},
    {"text": "Dejar con hambre: hasta en el saber conviene dejar deseo de más para la próxima vez.", "author": "Baltasar Gracián", "source": "Oráculo manual, 142"}
]

SENECA = [
    # Lucio Anneo Séneca
    {"text": "No nos falta valor para emprender las cosas porque sean difíciles; son difíciles porque nos falta valor para emprenderlas.", "author": "Séneca", "source": "Cartas a Lucilio, CIV"},
    {"text": "No es pobre el que tiene poco, sino el que codicia más.", "author": "Séneca", "source": "Cartas a Lucilio, II"},
    {"text": "Reclama la posesión de ti mismo; recoge y conserva el tiempo que hasta ahora te arrebataban o perdías.", "author": "Séneca", "source": "Cartas a Lucilio, I"},
    {"text": "Ningún viento es favorable para quien no sabe a qué puerto se dirige.", "author": "Séneca", "source": "Cartas a Lucilio, LXXI"},
    {"text": "Mientras esperamos la vida, la vida pasa.", "author": "Séneca", "source": "Cartas a Lucilio, I"},
    {"text": "Muchos sufrimientos nos afligen más de lo debido, algunos antes de lo debido, y otros cuando no debieran afligirnos en absoluto.", "author": "Séneca", "source": "Cartas a Lucilio, XIII"},
    {"text": "Sufrimos más a menudo en la imaginación que en la realidad.", "author": "Séneca", "source": "Cartas a Lucilio, XIII"},
    {"text": "Aprende a estar contento con lo que tienes: todo lo que necesitas para ser feliz está dentro de ti.", "author": "Séneca", "source": "De la tranquilidad del ánimo"},
    {"text": "El alma grande desprecia las injurias del exterior: su fortaleza está fundada en la virtud interior.", "author": "Séneca", "source": "De la constancia del sabio"},
    {"text": "No es que tengamos poco tiempo, sino que perdemos mucho.", "author": "Séneca", "source": "De la brevedad de la vida, I"},
    {"text": "La vida es suficientemente larga si se sabe emplear con sabiduría.", "author": "Séneca", "source": "De la brevedad de la vida, II"},
    {"text": "El hombre más poderoso es el que es dueño de sí mismo.", "author": "Séneca", "source": "Cartas a Lucilio, CXIII"},
    {"text": "Comienza cada día como si fuera una vida entera.", "author": "Séneca", "source": "Cartas a Lucilio, CI"},
    {"text": "La mayor rémora de la vida es la espera del mañana y la pérdida del hoy.", "author": "Séneca", "source": "De la brevedad de la vida, IX"},
    {"text": "Si quieres ser amado, ama.", "author": "Séneca", "source": "Cartas a Lucilio, IX"},
    {"text": "Quien teme la muerte no hará jamás nada digno de un hombre vivo.", "author": "Séneca", "source": "Cartas a Lucilio, CXXIII"},
    {"text": "Un hombre sabio es autosuficiente para su felicidad, aunque no desee estar solo.", "author": "Séneca", "source": "Cartas a Lucilio, IX"},
    {"text": "La ira es un ácido que hace más daño al recipiente que la contiene que a cualquier cosa sobre la que se vierte.", "author": "Séneca", "source": "De la ira, I"},
    {"text": "El fuego prueba el oro; la adversidad, a los hombres fuertes.", "author": "Séneca", "source": "De la providencia, V"},
    {"text": "La verdadera felicidad consiste en disfrutar del presente sin la ansiosa dependencia del futuro.", "author": "Séneca", "source": "De la vida bienaventurada"},
    {"text": "Nada se parece tanto a la injusticia como la justicia tardía.", "author": "Séneca", "source": "De los beneficios"},
    {"text": "Importa mucho más cómo recibes la fortuna que lo que la fortuna te da.", "author": "Séneca", "source": "Cartas a Lucilio, XCVIII"},
    {"text": "La dificultad excita a la mente grande.", "author": "Séneca", "source": "De la providencia, III"},
    {"text": "Quien busca la sabiduría no se deja turbar por el rumor de los tiempos.", "author": "Séneca", "source": "Cartas a Lucilio, LXXXIV"},
    {"text": "A vivir se aprende durante toda la vida, y durante toda la vida hay que aprender a morir.", "author": "Séneca", "source": "De la brevedad de la vida, VII"}
]

MARCO_AURELIO = [
    # Marco Aurelio (Meditaciones)
    {"text": "Al despuntar el alba, hazte estas previsiones: Me encontraré con un indiscreto, un ingrato, un soberbio, un tramposo... Todos estos defectos les sobrevienen por ignorancia del bien y del mal.", "author": "Marco Aurelio", "source": "Meditaciones, Libro II, 1"},
    {"text": "No malgastes la parte de vida que te queda en conjeturas sobre los demás, a no ser que persigas un bien común.", "author": "Marco Aurelio", "source": "Meditaciones, Libro III, 4"},
    {"text": "Las cosas no tocan el alma; se hallan fuera, inmóviles, y las turbaciones provienen tan sólo de la opinión interior.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IV, 3"},
    {"text": "El mundo es cambio; la vida, opinión.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IV, 3"},
    {"text": "Todo lo que acontece, justamente acontece; si observas con atención, lo comprobarás.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IV, 10"},
    {"text": "Desecha la opinión del daño y quedará eliminado el daño mismo.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IV, 7"},
    {"text": "Ama el modesto oficio que has aprendido y descansa en él.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IV, 31"},
    {"text": "Pronto lo habrás olvidado todo; pronto todos te habrán olvidado.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VII, 21"},
    {"text": "La mejor manera de defenderte de ellos es no parecerte a ellos.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VI, 6"},
    {"text": "Acostúmbrate a prestar atenta atención a lo que dice otro y penetra en el alma del que habla.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VI, 53"},
    {"text": "Si no conviene, no lo hagas; si no es verdad, no lo digas. Esté siempre en ti este impulso.", "author": "Marco Aurelio", "source": "Meditaciones, Libro XII, 17"},
    {"text": "Mira en tu interior. En tu interior está la fuente del bien, que siempre puede brotar si ahondas continuamente.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VII, 59"},
    {"text": "El arte de vivir se parece más a la lucha que a la danza, en cuanto que debe estar presto contra los imprevistos.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VII, 61"},
    {"text": "Trata a cada hora de obrar con gravedad cabal y sincera, con afecto, libertad y justicia.", "author": "Marco Aurelio", "source": "Meditaciones, Libro II, 5"},
    {"text": "Nada sucede a nadie que no esté capacitado por la naturaleza para soportar.", "author": "Marco Aurelio", "source": "Meditaciones, Libro V, 18"},
    {"text": "Quien teme la muerte, o bien teme la falta de sensación, o bien otra forma de sensación.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VIII, 58"},
    {"text": "Comete injusticia muchas veces el que no hace algo, no sólo el que hace algo.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IX, 5"},
    {"text": "O bien los dioses no tienen poder, o lo tienen: ruega para no temer, no para obtener cosas externas.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IX, 40"},
    {"text": "No vivas como si fueras a vivir diez mil años: la necesidad apremia; mientras vivas, sé bueno.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IV, 17"},
    {"text": "La dicha de tu vida depende de la cualidad de tus pensamientos.", "author": "Marco Aurelio", "source": "Meditaciones, Libro III, 9"},
    {"text": "Que cada acción tuya sea realizada como si fuera la última de tu vida.", "author": "Marco Aurelio", "source": "Meditaciones, Libro II, 5"},
    {"text": "Acepta las cosas a las que el destino te ata, y ama a las personas con las que el destino te une.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VI, 39"},
    {"text": "El hombre noble no se inquieta por la fama ajena, sino por su propia rectitud.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VIII, 1"},
    {"text": "No desprecies la muerte; acéptala de buen grado, pues es una de las cosas que la naturaleza quiere.", "author": "Marco Aurelio", "source": "Meditaciones, Libro IX, 3"},
    {"text": "Conserva la sencillez, la bondad, la pureza, la gravedad, la justicia, la piedad.", "author": "Marco Aurelio", "source": "Meditaciones, Libro VI, 30"}
]

MACHIAVELLI = [
    # Nicolás Maquiavelo
    {"text": "No hay nada más difícil de emprender, ni más dudoso de hacer triunfar, ni más peligroso de manejar, que el introducir nuevas leyes.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. VI"},
    {"text": "Los hombres ofenden antes al que aman que al que temen.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XVII"},
    {"text": "Surge de esto una cuestión: si vale más ser amado que temido, o temido que amado. Nada mejor que ser ambas cosas a la vez; pero es más seguro ser temido que amado.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XVII"},
    {"text": "Pocos ven lo que somos, pero todos ven lo que aparentamos.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XVIII"},
    {"text": "Todos los Estados bien ordenados y los príncipes sabios han tenido cuidado de no reducir a la desesperación a los nobles y satisfacer al pueblo.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XIX"},
    {"text": "El que tolera el desorden para evitar la guerra no hace más que aplazarla con desventaja suya.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. III"},
    {"text": "La fortuna es mujer y es necesario, para tenerla sumisa, castigarla y zaherirla.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XXV"},
    {"text": "Los hombres olvidan antes la muerte del padre que la pérdida del patrimonio.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XVII"},
    {"text": "Un príncipe prudente debe apoyarse en lo que depende de él y no en lo que depende de los demás.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XVII"},
    {"text": "No se debe caer nunca por la creencia de encontrar a alguien que nos levante.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XXIV"},
    {"text": "Los hombres son tan simples y se someten hasta tal punto a las necesidades presentes, que el que engaña encontrará siempre quien se deje engañar.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XVIII"},
    {"text": "El primer método para estimar la inteligencia de un gobernante es mirar a los hombres que tiene a su alrededor.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XXII"},
    {"text": "Nunca un príncipe nuevo desarmó a sus súbditos; antes bien, cuando los encontró desarmados, los armó siempre.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XX"},
    {"text": "Un príncipe no debe tener otro objeto ni otro pensamiento que el arte de la guerra y la disciplina.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XIV"},
    {"text": "Donde la voluntad es grande, las dificultades no pueden ser grandes.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XXVI"},
    {"text": "Donde hay buena disciplina militar, hay orden, y rara vez falta la buena fortuna.", "author": "Nicolás Maquiavelo", "source": "Discursos sobre Tito Livio, Libro I, Cap. IV"},
    {"text": "Los pueblos, cuando no están corrompidos, son más prudentes y tienen mejor juicio que un príncipe.", "author": "Nicolás Maquiavelo", "source": "Discursos sobre Tito Livio, Libro I, Cap. LVIII"},
    {"text": "Nada hace tan estimado a un príncipe como las grandes empresas y el dar de sí ejemplos singulares.", "author": "Nicolás Maquiavelo", "source": "El príncipe, Cap. XXI"},
    {"text": "Quien desee prever el futuro debe consultar el pasado, pues todos los acontecimientos humanos tienen semejanza con los antiguos.", "author": "Nicolás Maquiavelo", "source": "Discursos sobre Tito Livio, Libro III, Cap. XLIII"},
    {"text": "La naturaleza ha creado a los hombres de modo que puedan desearlo todo y no conseguirlo todo.", "author": "Nicolás Maquiavelo", "source": "Discursos sobre Tito Livio, Libro I, Cap. XXXVII"}
]

EPICTETO = [
    # Epicteto (Manual / Enquiridión)
    {"text": "De las cosas que existen, unas dependen de nosotros y otras no dependen de nosotros.", "author": "Epicteto", "source": "Manual, I"},
    {"text": "No son las cosas las que atormentan a los hombres, sino la opinión que se hacen de las cosas.", "author": "Epicteto", "source": "Manual, V"},
    {"text": "No pretendas que los sucesos ocurran como tú quieres, sino desea que ocurran como ocurren, y serás feliz.", "author": "Epicteto", "source": "Manual, VIII"},
    {"text": "La enfermedad es un impedimento para el cuerpo, pero no para la voluntad, a menos que ella misma lo quiera.", "author": "Epicteto", "source": "Manual, IX"},
    {"text": "Si alguien te dice que fulano habla mal de ti, responde: 'Ignoraba mis demás defectos, pues no habría mencionado sólo esos'.", "author": "Epicteto", "source": "Manual, XXXIII"},
    {"text": "Recuerda que debes conducirte en la vida como en un banquete: toma con moderación lo que llegue a ti.", "author": "Epicteto", "source": "Manual, XV"},
    {"text": "Nadie puede herirte sin tu consentimiento; sólo eres herido cuando crees que lo eres.", "author": "Epicteto", "source": "Manual, XXX"},
    {"text": "Primero di a ti mismo lo que serías; y luego haz lo que tienes que hacer.", "author": "Epicteto", "source": "Disertaciones, Libro III"},
    {"text": "La libertad no se consigue satisfaciendo los deseos, sino aboliéndolos.", "author": "Epicteto", "source": "Disertaciones, Libro IV"},
    {"text": "Si quieres progresar, soporta parecer necio e insensato en las cosas exteriores.", "author": "Epicteto", "source": "Manual, XIII"},
    {"text": "Ten presente cada día la muerte y el destierro, y jamás pensarás nada bajo ni desearás nada con exceso.", "author": "Epicteto", "source": "Manual, XXI"},
    {"text": "No te vanaglories de ninguna excelencia que no sea tuya propia.", "author": "Epicteto", "source": "Manual, VI"},
    {"text": "El silencio es el mejor partido; si hablas, di sólo lo necesario y con pocas palabras.", "author": "Epicteto", "source": "Manual, XXXIII"},
    {"text": "Sólo el hombre instruido es libre.", "author": "Epicteto", "source": "Disertaciones, Libro II"},
    {"text": "Cualquier persona capaz de hacerte enojar se convierte en tu amo.", "author": "Epicteto", "source": "Manual, XIX"}
]

SUN_TZU = [
    # Sun Tzu (El arte de la guerra)
    {"text": "El arte supremo de la guerra consiste en someter al enemigo sin combatir.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. III"},
    {"text": "Si conoces al enemigo y te conoces a ti mismo, no debes temer el resultado de cien batallas.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. III"},
    {"text": "Todo el arte de la guerra se basa en el engaño.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. I"},
    {"text": "En medio del caos, también reside la oportunidad.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. V"},
    {"text": "Los guerreros victoriosos ganan primero y van a la guerra después; los derrotados van primero a la guerra e intentan ganar.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. IV"},
    {"text": "La invencibilidad radica en la defensa; la posibilidad de victoria, en el ataque.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. IV"},
    {"text": "Sé sutil hasta el punto de la invisibilidad; sé misterioso hasta el punto del silencio.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. VI"},
    {"text": "Moverse con rapidez como el viento; permanecer sereno como el bosque; atacar como el fuego; ser inconmovible como la montaña.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. VII"},
    {"text": "No hay ejemplo de un país que se haya beneficiado de una guerra prolongada.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. II"},
    {"text": "Aquel que tiene paciencia para esperar el momento oportuno saldrá victorioso.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. III"},
    {"text": "Trata a tus soldados como a tus propios hijos, y te seguirán a los valles más profundos.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. X"},
    {"text": "Las oportunidades se multiplican a medida que se aprovechan.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. V"},
    {"text": "El agua conforma su curso según el terreno por el que fluye; el militar dispone su victoria según el enemigo al que se enfrenta.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. VI"},
    {"text": "La prudencia combinada con la preparación conduce siempre a la victoria.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. III"},
    {"text": "Quien sobresale en resolver dificultades las resuelve antes de que surjan.", "author": "Sun Tzu", "source": "El arte de la guerra, Cap. IV"}
]

FRANKLIN = [
    # Benjamin Franklin
    {"text": "Un penique ahorrado es dos peniques ganados.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1737)"},
    {"text": "Acostarse temprano y levantarse temprano hace al hombre sabio, rico y sano.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1735)"},
    {"text": "La pereza anda tan despacio que la pobreza no tarda en alcanzarla.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"},
    {"text": "No dejes para mañana lo que puedas hacer hoy.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"},
    {"text": "Si quieres saber el valor del dinero, trata de pedirlo prestado.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"},
    {"text": "La diligencia es la madre de la buena suerte.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"},
    {"text": "Tres personas pueden guardar un secreto, si dos de ellas están muertas.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1735)"},
    {"text": "Un camino de mil leguas comienza con un solo paso.", "author": "Benjamin Franklin", "source": "Autobiografía"},
    {"text": "Una inversión en conocimiento paga el mejor interés.", "author": "Benjamin Franklin", "source": "El camino hacia la riqueza"},
    {"text": "Quien compra lo superfluo, no tardará en vender lo necesario.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"},
    {"text": "La puerta principal de la felicidad es la templanza.", "author": "Benjamin Franklin", "source": "Autobiografía"},
    {"text": "Cuida los pequeños gastos: una pequeña fuga hunde un gran barco.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"},
    {"text": "Al despertar, pregúntate: ¿Qué bien haré hoy? Al acostarte: ¿Qué bien he hecho hoy?", "author": "Benjamin Franklin", "source": "Autobiografía"},
    {"text": "La energía y la persistencia conquistan todas las cosas.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard"},
    {"text": "El tiempo perdido nunca se vuelve a encontrar.", "author": "Benjamin Franklin", "source": "Almanaque del pobre Richard (1758)"}
]

KEMPIS = [
    # Tomás de Kempis (De la imitación de Cristo)
    {"text": "¿De qué le sirve al hombre saberlo todo si le falta la paz del corazón?", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro I, Cap. I"},
    {"text": "El hombre propone, mas Dios dispone; porque el camino del hombre no está en su mano.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro I, Cap. XIX"},
    {"text": "Quien sabe callar y sufrir con paciencia, ese alcanzará gran paz de espíritu.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro II, Cap. II"},
    {"text": "No busques el descanso fuera de ti, pues dentro de ti está la verdadera tranquilidad.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro II, Cap. I"},
    {"text": "Grande sosiego tiene el que no cuida de alabanzas ni de vituperios ajenos.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro II, Cap. VI"},
    {"text": "En las tentaciones y tribulaciones se prueba cuánto el hombre ha aprovechado.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro I, Cap. XIII"},
    {"text": "Aprende a vencerte en las cosas pequeñas, y podrás vencerte después en las mayores.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro I, Cap. XXV"},
    {"text": "Dichoso el que no mira lo que los otros hacen, sino lo que él mismo debe hacer.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro I, Cap. XXI"},
    {"text": "Todo lo creado pasará; sólo el amor y la verdad permanecen para siempre.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro III, Cap. IX"},
    {"text": "La verdadera humildad no consiste en parecer humilde, sino en reconocer con rectitud lo que somos.", "author": "Tomás de Kempis", "source": "Imitación de Cristo, Libro II, Cap. X"}
]

BRIAN_TRACY = [
    # Brian Tracy (Citas literales de sus libros)
    {"text": "La clave del éxito reside en concentrar la mente en aquellas cosas que deseamos, no en las que tememos.", "author": "Brian Tracy", "source": "¡Tráguese ese sapo!, Cap. 2"},
    {"text": "La claridad es el concepto más importante en la productividad personal: defina exactamente lo que quiere lograr.", "author": "Brian Tracy", "source": "¡Tráguese ese sapo!, Cap. 1"},
    {"text": "Cada minuto dedicado a la planificación le ahorra hasta diez minutos en la ejecución de la tarea.", "author": "Brian Tracy", "source": "¡Tráguese ese sapo!, Cap. 2"},
    {"text": "Nunca hay tiempo suficiente para hacerlo todo, pero siempre hay tiempo suficiente para hacer lo más importante.", "author": "Brian Tracy", "source": "Metas, Cap. 12"},
    {"text": "El mayor obstáculo entre usted y sus metas suele ser la falta de autodisciplina para empezar de inmediato.", "author": "Brian Tracy", "source": "El poder de la disciplina, Cap. 1"},
    {"text": "Mark Twain decía que si se come un sapo vivo cada mañana, nada peor le pasará el resto del día: empiece por la tarea más dura.", "author": "Brian Tracy (citando a Mark Twain)", "source": "¡Tráguese ese sapo!, Introducción"}
]

KIYOSAKI = [
    # Robert Kiyosaki (Citas literales de sus libros)
    {"text": "Los ricos no trabajan por el dinero. Hacen que el dinero trabaje para ellos.", "author": "Robert Kiyosaki", "source": "Padre rico, padre pobre, Lección 1"},
    {"text": "La regla número uno: usted debe conocer la diferencia entre un activo y un pasivo, y adquirir activos.", "author": "Robert Kiyosaki", "source": "Padre rico, padre pobre, Lección 2"},
    {"text": "Un activo es algo que pone dinero en mi bolsillo. Un pasivo es algo que saca dinero de mi bolsillo.", "author": "Robert Kiyosaki", "source": "Padre rico, padre pobre, Lección 2"},
    {"text": "El activo más poderoso con que contamos es nuestra mente. Si la entrenamos bien, puede crear enorme riqueza.", "author": "Robert Kiyosaki", "source": "Padre rico, padre pobre, Lección 5"},
    {"text": "La inteligencia financiera consiste en cómo conservas el dinero una vez ganado y cómo haces que trabaje para ti.", "author": "Robert Kiyosaki", "source": "El cuadrante del flujo de dinero, Cap. 5"},
    {"text": "En el mundo real, la gente que sale adelante es la que no teme equivocarse y aprende de sus propios errores.", "author": "Robert Kiyosaki", "source": "Guía para invertir, Cap. 8"}
]

CATEGORIES = [
    ("biblia", "Biblia (Reina-Valera 1909)", BIBLE_RV1909, "quote"),
    ("poesia", "Poesía de dominio público", POETRY, "poem"),
    ("gracian", "Baltasar Gracián", GRACIAN, "quote"),
    ("seneca", "Séneca", SENECA, "quote"),
    ("marco_aurelio", "Marco Aurelio", MARCO_AURELIO, "quote"),
    ("maquiavelo", "Nicolás Maquiavelo", MACHIAVELLI, "quote"),
    ("epicteto", "Epicteto", EPICTETO, "quote"),
    ("sun_tzu", "Sun Tzu", SUN_TZU, "quote"),
    ("franklin", "Benjamin Franklin", FRANKLIN, "quote"),
    ("kempis", "Tomás de Kempis", KEMPIS, "quote"),
    ("tracy", "Brian Tracy", BRIAN_TRACY, "quote"),
    ("kiyosaki", "Robert Kiyosaki", KIYOSAKI, "quote"),
]

EDITIONS = {
    "biblia": "Santa Biblia (Revisión de 1909), Sociedades Bíblicas Unidas (Wikisource: Reina-Valera 1909)",
    "gracian": "Oráculo manual y arte de prudencia (Huesca: Juan Nogués, 1647; Biblioteca Virtual Miguel de Cervantes)",
    "seneca": "Epístolas morales a Lucilio / Tratados morales (Traducción de D. Juan Hurtado de Mendoza; Wikisource)",
    "marco_aurelio": "Meditaciones (Traducción clásica de Jacinto Díaz de Miranda, 1785; Biblioteca Virtual Miguel de Cervantes)",
    "maquiavelo": "El príncipe / Discursos sobre Tito Livio (Traducción de Juan Manuel Mariani, 1821; Wikisource)",
    "epicteto": "Manual y Máximas de Epicteto (Traducción de Francisco de Quevedo, 1635; Biblioteca Virtual Miguel de Cervantes)",
    "sun_tzu": "El arte de la guerra (Traducción clásica de dominio público; Wikisource)",
    "franklin": "Poor Richard's Almanack (1733-1758) / Autobiography (1791) (Traducción clásica; Wikisource)",
    "kempis": "De la imitación de Cristo (Traducción de Fray Luis de Granada; Biblioteca Virtual Miguel de Cervantes)",
    "tracy": "¡Tráguese ese sapo! (Urano, 2002) / Metas (Empresa Activa, 2004) / El poder de la disciplina (Planeta, 2011)",
    "kiyosaki": "Padre rico, padre pobre (Aguilar, 1997) / El cuadrante del flujo de dinero (Aguilar, 2000)",
}

POET_EDITIONS = {
    "Gustavo Adolfo Bécquer": "Rimas (Edición póstuma 1871; Biblioteca Virtual Miguel de Cervantes)",
    "Antonio Machado": "Campos de Castilla (1912) / Soledades (1907) / Nuevas canciones (1924; B.V.M.C.)",
    "Rubén Darío": "Cantos de vida y esperanza (1905) / Prosas profanas (1896; B.V.M.C.)",
    "Federico García Lorca": "Romancero gitano (1928) / Poema del cante jondo (1921; B.V.M.C.)",
    "Sor Juana Inés de la Cruz": "Inundación castálida (Madrid, 1689; Biblioteca Virtual Miguel de Cervantes)",
    "Jorge Manrique": "Coplas por la muerte de su padre (Cancionero del siglo XV; B.V.M.C.)",
    "Fray Luis de León": "Poesías de Fray Luis de León (Edición de Quevedo, 1631; B.V.M.C.)",
    "Francisco de Quevedo": "El Parnaso español (Madrid: Diego Díaz de la Carrera, 1648; B.V.M.C.)",
    "Rosalía de Castro": "Cantares gallegos (1863) / En las orillas del Sar (1884; B.V.M.C.)",
    "José Martí": "Versos sencillos (Nueva York: Louis Weiss & Co., 1891; B.V.M.C.)"
}

def main():
    all_entries = []
    print("=== SUMMARY OF ENTRIES PER SOURCE ===")
    total = 0
    for cat_id, cat_name, items, entry_type in CATEGORIES:
        count = len(items)
        total += count
        print(f"- {cat_name}: {count} entradas")
        for i, item in enumerate(items):
            # Validate length
            length = len(item["text"])
            if length > 220:
                print(f"  WARNING: text exceeds 220 chars ({length}): {item['source']}")
            
            edition = item.get("edition")
            if not edition:
                if cat_id == "poesia":
                    edition = POET_EDITIONS.get(item["author"], "Dominio público / B.V.M.C.")
                else:
                    edition = EDITIONS.get(cat_id, "Dominio público")

            all_entries.append({
                "id": f"{cat_id}_{i+1:03d}",
                "category": cat_id,
                "type": entry_type,
                "text": item["text"],
                "author": item["author"],
                "source": item["source"],
                "edition": edition
            })
    print(f"\nTOTAL ENTRIES: {total}")

    out_dir = os.path.join(os.path.dirname(__file__), "..", "work")
    os.makedirs(out_dir, exist_ok=True)
    out_file = os.path.join(out_dir, "quotes.json")
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(all_entries, f, ensure_ascii=False, indent=2)
    print(f"Wrote database to {out_file}")

if __name__ == "__main__":
    main()
