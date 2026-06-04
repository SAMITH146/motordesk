<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css?v=1.2" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css?v=1.2" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.2" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css?v=1.2" />
            <title>Dashboard Administrador | MotorDesk</title>
        </head>

        <body>
                <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
        </div>
        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item active">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProveedorController" class="navbar__menu-item">Proveedores</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>
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

            <main class="admin-main fade-in">
                <section class="admin-section">
                    <header>
                        <h1 class="admin-section__title">Bienvenido, ${sessionScope.usuarioLogueado.nombre}</h1>
                        <p class="admin-section__subtitle">Aquí tienes un resumen rápido de MotorDesk hoy.</p>
                    </header>

                    <div class="admin-dashboard__grid">
                        <article class="admin-card">
                    <div class="admin-dashboard__grid" style="grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));">
                        
                        <article class="admin-card card-warning" style="grid-column: 1 / -1; display:flex; justify-content:space-between; align-items:center; padding: 1.5rem;">
                            <div>
                                <h3 class="admin-card__title" style="margin:0;">Stock Bajo/Crítico</h3>
                                <div class="admin-card__value text-warning" style="font-size: 1.2rem; margin-top:5px;">
                                    <c:out value="${not empty requestScope.stockCritico ? requestScope.stockCritico : '0'}" /> Productos Críticos
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/ProductoController" class="admin-btn" style="background:#e74c3c; border-color:#e74c3c;">Ir a Inventario</a>
                        </article>

                        <article class="admin-card">
                            <div class="admin-card__header" style="border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; margin-bottom: 10px;">
                                <h3 class="admin-card__title">Últimos Mecánicos Agregados</h3>
                                <div class="admin-card__icon">🧑‍🔧</div>
                            </div>
                            <div>
                                <c:choose>
                                    <c:when test="${not empty requestScope.recentMecanicos}">
                                        <c:forEach var="m" items="${requestScope.recentMecanicos}">
                                            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid rgba(255,255,255,0.05); font-size:0.95rem;">
                                                <span>${m.nombre}</span>
                                                <span class="status-badge status-badge--${m.estadoEmpleado.toLowerCase()}">${m.estadoEmpleado}</span>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <p style="opacity: 0.5; font-size: 0.9rem; text-align: center; padding: 10px 0;">No hay datos recientes.</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <a href="${pageContext.request.contextPath}/MecanicoController" class="admin-card__link" style="text-align: center; margin-top: 15px; display: block;">Administrar Plantilla →</a>
                        </article>

                        <article class="admin-card">
                            <div class="admin-card__header" style="border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; margin-bottom: 10px;">
                                <h3 class="admin-card__title">Órdenes de Trabajo Recientes</h3>
                                <div class="admin-card__icon">📋</div>
                            </div>
                            <div>
                                <c:choose>
                                    <c:when test="${not empty requestScope.recentOrdenes}">
                                        <c:forEach var="ord" items="${requestScope.recentOrdenes}">
                                            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid rgba(255,255,255,0.05); font-size:0.95rem;">
                                                <span>Placa: <strong>${ord.placaVehiculo}</strong></span>
                                                <span class="status-badge status-badge--${ord.estado.toLowerCase()}">${ord.estado}</span>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <p style="opacity: 0.5; font-size: 0.9rem; text-align: center; padding: 10px 0;">No hay órdenes recientes.</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="admin-card__link" style="text-align: center; margin-top: 15px; display: block;">Gestionar Todas las Órdenes →</a>
                        </article>

                    </div>
                </section>
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
        </body>
        </html>

