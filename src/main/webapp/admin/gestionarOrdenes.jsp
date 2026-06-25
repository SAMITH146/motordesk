<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css?v=1.2" />
    <title>Gestión Global de Órdenes | MotorDesk</title>
</head>
<body>
        <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
        </div>
        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProveedorController" class="navbar__menu-item">Proveedores</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item active">Órdenes</a>

            <a href="${pageContext.request.contextPath}/BitacoraController" class="navbar__menu-item">Auditoría</a>
        </nav>
        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesión" class="navbar__session-icon" />
            </a>
        </div>
    </header>

                                                <option value="ESPERA" ${ord.estado == 'ESPERA' ? 'selected' : ''}>En Espera</option>
                                                <option value="TERMINADO" ${ord.estado == 'TERMINADO' ? 'selected' : ''}>Terminado</option>
                                                <option value="FACTURADO" ${ord.estado == 'FACTURADO' ? 'selected' : ''}>Facturada</option>
                                            </select>

                                            <div id="espera-fields-${ord.idOrden}" style="margin-top: 10px; display: ${ord.estado == 'ESPERA' ? 'block' : 'none'};">
                                                <!-- Aquí renderizamos el input donde el usuario escribirá el motivo de espera de la orden -->
                                                <input type="text" name="motivo" class="form-input" placeholder="Motivo (solo si es Espera)" value="${ord.motivoEspera}" style="margin-bottom: 5px;" />
                                                <!-- Aquí renderizamos el input para el tiempo de espera estimado -->
                                                <input type="text" name="tiempo" class="form-input" placeholder="Tiempo (ej: 1h)" value="${ord.tiempoEspera}" />
                                            </div>

                                            <button type="submit" class="admin-btn" style="width: 100%; margin-top: 10px;">Guardar</button>
                                            <a href="?action=listAll&filtro=${empty param.filtro ? 'TODAS' : param.filtro}" class="admin-btn admin-btn--danger" style="display:block; text-align:center; text-decoration:none; width: 100%; margin-top: 10px;">Cancelar</a>
                                        </form>
                                    </div>
                                    </c:if>
                                </c:if>

                                <a href="${pageContext.request.contextPath}/OrdenController?action=verFactura&id_orden=${ord.idOrden}" class="admin-btn admin-btn--small" style="display:block; text-align:center; text-decoration:none; width: 100%; margin-top: 8px; background: rgba(59,130,246,0.15); border: 1px solid rgba(59,130,246,0.4); color: #3b82f6;">Ver Factura</a>
                            </article>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p style="grid-column: 1/-1; text-align: center; opacity: 0.5; padding: 4rem;">Sin órdenes registradas.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </main>

    </main>

    <!-- Log out Modal (CSS Only) -->
    <div id="logoutModal" class="modal-css">
        <div class="modal-content-css">
            <h2>¿Cerrar Sesión?</h2>
            <p>Estás a punto de salir del sistema.<br>¿Estás seguro?</p>
            <div class="modal-buttons-css">
                <a href="#" class="btn-modal-css btn-modal-css--cancel">No, quedarme</a>
                <a href="${pageContext.request.contextPath}/LogoutController" class="btn-modal-css btn-modal-css--confirm">Sí, salir</a>
            </div>
        </div>
    </div>

    <script>
        function toggleEspera(selectElement, idOrden) {
            var fields = document.getElementById('espera-fields-' + idOrden);
            if (selectElement.value === 'ESPERA') {
                fields.style.display = 'block';
            } else {
                fields.style.display = 'none';
            }
        }
    </script>
</body>
</html>

