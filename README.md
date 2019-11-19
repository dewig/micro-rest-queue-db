# micro-rest-queue-db

## API

La aplicacion expone dos endpoints:

* /mutant/, un POST que ejecuta el metodo checking-service/src/main/java/base/MutantTester.java::isMutant
sobre su payload y que devuelve un 200 si el metodo retorna true y un 403 (forbidden) si devuelve false.\
El POST debe tener el siguiente formato :
  * Headers: Content-Type = application/json
  * Body: {"dna": ["aaaa", "aaaa", "aaaa", "aaaa"]}, la longitud de cada string
  debe ser igual a la cantidad de strings. 

* /stats/, un GET que devuelve los stats:\
{"count_mutant_dna": 16, "count_human_dna": 16, "ratio": 1}

El metodo isMutant retorna true si se encuentra mas de una secuencia de
cuatro letras iguales​, de forma oblicua, horizontal o vertical; y false
en cualquier otro caso.

## Arquitectura de la aplicacion
En architecture.png se puede ver la arquitectura de la aplicacion.

El sistema cloud se encarga del mecanismo de load-balancing y service discovery.

Los bloques M representan las posibles multiples instancias del microservicio
encargado de atender los requests a los endpoints mencionados.
El payload del metodo POST y el resultado del metodo isMutant sobre este,
se envian a dos colas distintas. EL metodo GET obtiene los valores desde
una base de datos (STATS), cacheandolos durante un tiempo.

Los bloques A representan las posibles multiples instancias del microservicio
encargado de tomar las muestras de adn que fueron depositadas en cola y
persistirlas en una base de datos (ADN).

El bloque S representa al microservicio que se encarga de contabilizar los
resultados depositados en la cola y persistir esa informacion en una base
de datos (STATS).