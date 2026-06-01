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
        <nav class="navbar__menu">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item active">Órdenes</a>
        </nav>
        <div class="navbar__session">
            <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesión" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">
        <header class="admin-section__header">
            <h1 class="admin-section__title">Gestión Global de Órdenes (Trabajo)</h1>
            <p class="admin-section__subtitle">Monitoreo histórico y administrativo de todos los servicios del taller.</p>
        </header>

        <section class="admin-section">
            <div class="tabs" style="margin-bottom: 2rem; display: flex; flex-wrap: wrap; gap: 0.5rem;">
                <a href="?action=listAll&filtro=TODAS" class="admin-btn ${empty param.filtro or param.filtro == 'TODAS' ? 'active' : ''}" style="text-decoration:none; text-align:center;">Todas</a>
                <a href="?action=listAll&filtro=ABIERTA" class="admin-btn ${param.filtro == 'ABIERTA' ? 'active' : ''}" style="text-decoration:none; text-align:center;">Abiertas</a>
                <a href="?action=listAll&filtro=PROCESO" class="admin-btn ${param.filtro == 'PROCESO' ? 'active' : ''}" style="text-decoration:none; text-align:center;">En Proceso</a>
                <a href="?action=listAll&filtro=ESPERA" class="admin-btn ${param.filtro == 'ESPERA' ? 'active' : ''}" style="text-decoration:none; text-align:center;">En Espera</a>
                <a href="?action=listAll&filtro=TERMINADO" class="admin-btn ${param.filtro == 'TERMINADO' ? 'active' : ''}" style="text-decoration:none; text-align:center;">Terminadas</a>
                <a href="?action=listAll&filtro=FACTURADO" class="admin-btn ${param.filtro == 'FACTURADO' ? 'active' : ''}" style="text-decoration:none; text-align:center;">Facturadas</a>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;" id="orders-grid">
                <c:choose>
                    <c:when test="${not empty requestScope.listaOrdenes}">
                        <c:forEach var="ord" items="${requestScope.listaOrdenes}">
                            <c:if test="${empty param.filtro or param.filtro == 'TODAS' or ord.estado == param.filtro}">
                            <article class="admin-card order-card" data-estado="${ord.estado}">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                                    <h4 style="font-size: 1.5rem; color: var(--accent-light); letter-spacing: 2px;">${not empty ord.placaVehiculo ? ord.placaVehiculo : 'S/P'}</h4>
                                    <span class="status-badge status-badge--${ord.estado.toLowerCase()}">${ord.estado}</span>
                                </div>
                                
                                <div style="font-size: 0.9rem; line-height: 1.4; margin-bottom: 1rem;">
                                    <p><strong>Descripción:</strong> ${ord.descripcion}</p>
                                    <p><strong>Mecánico:</strong> ${not empty ord.nombreMecanico ? ord.nombreMecanico : ord.docEmpleFk}</p>
                                    <p><strong>Total Orden:</strong> <fmt:formatNumber value="${ord.total}" type="currency" currencySymbol="$" /></p>
                                    <p style="opacity: 0.6; margin-top: 5px;">🕒 <fmt:formatDate value="${ord.fecha}" pattern="dd/MM/yyyy" /></p>
                                </div>

                                <c:if test="${ord.estado == 'ESPERA'}">
                                    <div style="background: rgba(241, 196, 15, 0.1); border: 1px dashed #f1c40f; padding: 10px; border-radius: 8px; margin-bottom: 1rem; font-size: 0.85rem;">
                                        <strong>Motivo:</strong> ${ord.motivoEspera}<br>
                                        <strong>Estimado:</strong> ${ord.tiempoEspera}
                                    </div>
                                </c:if>

                                <c:if test="${ord.estado ne 'FACTURADO'}">
                                    <a href="?action=listAll&filtro=${empty param.filtro ? 'TODAS' : param.filtro}&editar=${ord.idOrden}#orden-${ord.idOrden}" class="admin-btn admin-btn--small" id="orden-${ord.idOrden}" style="display:block; text-align:center; text-decoration:none; width: 100%;">Actualizar Estado</a>

                                    <c:if test="${param.editar == ord.idOrden}">
                                    <div style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid rgba(255,255,255,0.1);">
                                        <form action="${pageContext.request.contextPath}/OrdenController" method="post" class="admin-form">
                                            <input type="hidden" name="action" value="updateStatus" />
                                            <input type="hidden" name="id_orden" value="${ord.idOrden}" />
                                            
                                            <select name="nuevo_estado" class="form-input" required onchange="toggleEspera(this, ${ord.idOrden})">
                                                <option value="ABIERTA" ${ord.estado == 'ABIERTA' ? 'selected' : ''}>Abierta</option>
                                                <option value="PROCESO" ${ord.estado == 'PROCESO' ? 'selected' : ''}>En Proceso</option>
                                                <option value="ESPERA" ${ord.estado == 'ESPERA' ? 'selected' : ''}>En Espera</option>
                                                <option value="TERMINADO" ${ord.estado == 'TERMINADO' ? 'selected' : ''}>Terminado</option>
                                                <option value="FACTURADO" ${ord.estado == 'FACTURADO' ? 'selected' : ''}>Facturada</option>
                                            </select>

                                            <div id="espera-fields-${ord.idOrden}" style="margin-top: 10px; display: ${ord.estado == 'ESPERA' ? 'block' : 'none'};">
                                                <input type="text" name="motivo" class="form-input" placeholder="Motivo (solo si es Espera)" value="${ord.motivoEspera}" style="margin-bottom: 5px;" />
                                                <input type="text" name="tiempo" class="form-input" placeholder="Tiempo (ej: 1h)" value="${ord.tiempoEspera}" />
                                            </div>

                                            <button type="submit" class="admin-btn" style="width: 100%; margin-top: 10px;">Guardar</button>
                                            <a href="?action=listAll&filtro=${empty param.filtro ? 'TODAS' : param.filtro}" class="admin-btn admin-btn--danger" style="display:block; text-align:center; text-decoration:none; width: 100%; margin-top: 10px;">Cancelar</a>
                                        </form>
                                    </div>
                                    </c:if>
                                </c:if>
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
