PROXY POP3
-----------------------------------------------------------------------
Trabajo Practico Especial para la materia Protocolos de Comunicación.

<--Compilacion
-----------------------------------------------------------------------
	Para la compilacion se ejecuta

	$> ant compile -->

-----------------------------------------------------------------------
Ejecucion

	Para correrlo se ejecuta java -jar con el path del jar "proxy.jar" generado en ~build/jar.

	Soporta los parametroe -p -d -c siendo estos, respectivamente, source port, destination port y el puerto de configuracion remota.

	Si -p no se asigna toma por default 3000, si -d no se asigna
	toma por default 110, si -c no se asigna toma por default 51914

-----------------------------------------------------------------------
Configuracion inicial

La configuracion inicial para iniciar el proxy POP3 y el administrador se encuentra en el archivo Configuracion.xml. 
Este archivo no se modifica en tiempo de ejecución. Este XML se debe encontrar en la misma carpeta en la que se encuentra
el archivo .jar.