PROXY POP3
-----------------------------------------------------------------------
Trabajo Practico Especial para la materia Protocolos de COmin
Desarrollado para la catedra de Protocolos de Comunicacion del ITBA junto a Jorge Mozzino y Tomas Mehdi.

Compilacion
-----------------------------------------------------------------------
	Para la compilacion se ejecuta

	$> ant compile

-----------------------------------------------------------------------
Ejecucion

	Para correrlo se ejecuta java -jar con el path del jar "proxy.jar" generado en ~build/jar.

	Soporta los parametroe -p -d -c siendo estos, respectivamente, source port, destination port y el puerto de configuracion remota.

	Si -p no se asigna toma por default 3000, si -d no se asigna
	toma por default 110, si -c no se asigna toma por default 51914

-----------------------------------------------------------------------
Configuracion inicial

