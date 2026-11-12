# Reembolsos Integrado: solicitudes de reembolso de gastos corporativos

Post-contenido de la unidad 12 de Patrones de Diseño de Software (UDES). Nicolás Andrés Sánchez Villamizar.

Sistema para que el área de Finanzas reemplace su hoja de cálculo de reembolsos: los empleados registran una solicitud con la categoría del gasto, el monto, la fecha y sus documentos soporte, y el sistema la valida, calcula el monto aprobado y avisa a los interesados cuando cambia de estado.

## Diseño Propuesto

Esta sección la escribí antes de implementar cualquier patrón: está en el primer commit del repositorio, junto con el modelo de dominio y sin una sola clase de `procesamiento`. Para cada requisito busqué primero qué es lo que cambia y después qué patrón aísla justo eso.

| Requisito | Punto de variabilidad concreto | Patrón | Por qué ese patrón y no otro |
|---|---|---|---|
| **RF1.** Validación en varias etapas | El conjunto de verificaciones: hoy son tres (documentos, tope de la categoría y duplicados), y Finanzas ya anunció una cuarta (límite anual acumulado). La primera que falla decide el motivo del rechazo. | **Chain of Responsibility** | Cada verificación decide sola si rechaza o deja pasar la solicitud al siguiente eslabón, así que la verificación nueva se agrega como un eslabón más sin abrir las que ya existen. |
| **RF2.** Cálculo distinto por categoría | La fórmula del monto aprobado: tope diario por días de viaje, monto completo hasta un tope mensual, o 80 % hasta un tope anual. Se elige una sola vez, por la categoría. | **Strategy** | Son algoritmos intercambiables que responden la misma pregunta (cuánto se aprueba) y se eligen por un dato de entrada, así que cada categoría nueva es una clase nueva y las fórmulas existentes no se tocan. |
| **RF3.** Notificación simultánea | Quiénes reaccionan a un cambio de estado y cómo: el empleado siempre, Contabilidad solo con aprobaciones y auditoría siempre, para su registro. Y el número de interesados crece (ya viene el panel de métricas). | **Observer** | El servicio solo anuncia que el estado cambió y cada interesado decide qué hacer, así que el panel de métricas entra como un observador más sin tocar el servicio ni los otros tres. |

**Alternativas que consideré para cada requisito**

- **RF1.** Una `List<Validador>` recorrida con un `for` también funcionaría, y lo reconozco: la diferencia con la cadena es pequeña. Elijo la cadena porque el requisito describe *etapas* en orden, donde la primera que falla detiene el proceso, y porque así cada eslabón es una clase con una sola razón para cambiar. Descarto Decorator: ninguna verificación envuelve ni modifica el resultado de otra.
- **RF2.** Descarto Template Method, porque las tres fórmulas no comparten un esqueleto: lo único común es aplicar un tope al final. También descarto un `enum` con un método por constante, que fue lo que elegí en la unidad 11 para un conjunto de tipos *cerrado y estable*. Aquí el requisito dice lo contrario: vienen categorías nuevas cada semestre, y cada calculadora como bean propio se puede probar y agregar sola.
- **RF3.** Llamar a los tres notificadores directamente desde el servicio obligaría a editar el servicio con cada interesado nuevo, que es justo lo que el requisito quiere evitar.

**Cómo se componen.** Los tres patrones son independientes entre sí: viven en `procesamiento/validacion`, `procesamiento/calculo` y `procesamiento/notificacion`, y solo `SolicitudReembolsoService` los coordina. Ninguno conoce a los otros dos ni a la base de datos. El servicio carga los datos y se los entrega ya listos.
