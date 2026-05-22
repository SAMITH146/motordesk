<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta name="description" content="Panel de control del mecanico en MotorDesk" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.1" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/mecanico-panel.css" />
    <title>Panel Mecanico | MotorDesk</title>
</head>
<body>
    <!-- ===== NAVBAR: solo logo + sesion (nav interno por tabs) ===== -->
    <header class="navbar navbar--compact">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
            <span class="navbar__app-name">MotorDesk</span>
        </div>
        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
                <span class="navbar__user-role">Mecanico</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesion" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">

        <!-- ===== SECTION: RESUMEN ===== -->
        <section id="resumen" class="mecanico-section" <c:if test="${not empty param.seccion and param.seccion ne 'resumen'}">style="display:none;"</c:if>>
            <header class="admin-section__header">
                <h1 class="admin-section__title">Bienvenido, ${sessionScope.usuarioLogueado.nombre}</h1>
                <p class="admin-section__subtitle">Aqui tienes el resumen de tu jornada en MotorDesk.</p>
            </header>

            <!-- Stat Cards -->
            <div class="stats-grid">
                <article class="stat-card">
                    <div class="stat-card__icon">&#128203;</div>
                    <div class="stat-card__content">
                        <div class="stat-card__title">Ordenes Abiertas</div>
                        <div class="stat-card__value">${not empty requestScope.ordenesAbiertas ? requestScope.ordenesAbiertas : '0'}</div>
                    </div>
                </article>
                <article class="stat-card">
                    <div class="stat-card__icon">&#128337;</div>
                    <div class="stat-card__content">
                        <div class="stat-card__title">Servicios Hoy</div>
                        <div class="stat-card__value">${not empty requestScope.ordenesHoy ? requestScope.ordenesHoy : '0'}</div>
                    </div>
                </article>
                <article class="stat-card stat-card--warning">
                    <div class="stat-card__icon">&#9888;</div>
                    <div class="stat-card__content">
                        <div class="stat-card__title">Alertas de Stock</div>
                        <div class="stat-card__value">${not empty requestScope.stockBajo ? requestScope.stockBajo.size() : '0'}</div>
                    </div>
                </article>
            </div>

            <!-- Quick Panels -->
            <div class="quick-grid">
                <!-- Recent Orders -->
                <div class="admin-card">
                    <div class="admin-card__header-row">
                        <h3 class="admin-card__title">Mis Ordenes Recientes</h3>
                        <a href="?seccion=ordenes" class="admin-btn admin-btn--small" style="text-decoration:none;">+ Nueva</a>
                    </div>
                    <div class="scroll-list">
                        <c:choose>
                            <c:when test="${not empty requestScope.listaOrdenes}">
                                <c:forEach var="ord" items="${requestScope.listaOrdenes}">
                                    <div class="order-mini-card">
                                        <div class="order-mini-card__top">
                                            <span class="order-mini-card__id">Orden #${ord.idOrden}</span>
                                            <span class="status-badge status-badge--${ord.estado.toLowerCase()}">${ord.estado}</span>
                                        </div>
                                        <p class="order-mini-card__desc">${ord.descripcion}</p>
                                        <span class="order-mini-card__date">
                                            <fmt:formatDate value="${ord.fecha}" pattern="dd/MM/yyyy" />
                                        </span>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">
                                    <span class="empty-state__icon">&#128203;</span>
                                    <p>No tienes ordenes abiertas.</p>
                                    <a href="?seccion=ordenes" class="admin-btn admin-btn--small" style="text-decoration:none;">Crear primera orden</a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Stock Alerts -->
                <div class="admin-card">
                    <h3 class="admin-card__title">Alertas de Inventario</h3>
                    <div class="scroll-list">
                        <c:choose>
                            <c:when test="${not empty requestScope.stockBajo}">
                                <c:forEach var="p" items="${requestScope.stockBajo}">
                                    <div class="stock-alert-item">
                                        <span class="stock-alert-item__name">${p.nombreProducto}</span>
                                        <span class="stock-alert-item__qty">${p.stock} uds.</span>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">
                                    <span class="empty-state__icon">&#10003;</span>
                                    <p>Inventario en buen estado.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </section>

        <!-- ===== SECTION: NUEVA ORDEN ===== -->
        <section id="ordenes" class="mecanico-section" <c:if test="${param.seccion ne 'ordenes'}">style="display:none;"</c:if>>
            <header class="admin-section__header">
                <h1 class="admin-section__title">Nueva Orden de Trabajo</h1>
                <p class="admin-section__subtitle">Registra un nuevo servicio del taller.</p>
            </header>

            <div class="admin-card" style="max-width: 750px;">
                <form action="${pageContext.request.contextPath}/OrdenController" method="post" class="admin-form" id="orderForm">
                    <input type="hidden" name="action" value="insert" />
                    <input type="hidden" name="id_mecanico" value="${sessionScope.usuarioLogueado.idEmpleado}" />

                    <div class="form-group">
                        <label class="form-label">Placa del Vehiculo</label>
                        <input type="text" name="placa" class="form-input" placeholder="Ej: ABC-123" required />
                    </div>

                    <div class="form-group">
                        <label class="form-label">Descripcion del Fallo / Servicio</label>
                        <textarea name="descripcion" class="form-input" style="height: 100px; resize: none;" placeholder="Describa el problema reportado..."></textarea>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Productos Utilizados</label>
                        <div id="repu-container" style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 10px;">
                            <c:forEach begin="1" end="5" var="i">
                            <div class="repu-row" style="display:grid; grid-template-columns:2fr 1fr; gap:10px; align-items:center;">
                                <select name="productos[]" class="form-input">
                                    <option value="">-- Seleccionar Repuesto --</option>
                                    <c:forEach var="p" items="${requestScope.listaProductos}">
                                        <option value="${p.idProducto}">${p.nombreProducto} - $<fmt:formatNumber value="${p.precioUnitario}" pattern="###,##0.00"/></option>
                                    </c:forEach>
                                </select>
                                <input type="number" name="cantidades[]" class="form-input" min="1" placeholder="Cant." />
                                <input type="hidden" name="precios[]" value="0" />
                            </div>
                            </c:forEach>
                        </div>
                    </div>

                    <div style="display:flex; gap: 1rem; margin-top: 0.5rem;">
                        <button type="submit" class="admin-btn">Abrir Orden de Trabajo</button>
                        <a href="?seccion=resumen" class="admin-btn admin-btn--danger" style="text-decoration:none; text-align:center;">Cancelar</a>
                    </div>
                </form>
            </div>
        </section>

        <!-- ===== SECTION: INVENTARIO ===== -->
        <section id="inventario" class="mecanico-section" <c:if test="${param.seccion ne 'inventario'}">style="display:none;"</c:if>>
            <header class="admin-section__header">
                <h1 class="admin-section__title">Inventario de Productos</h1>
                <p class="admin-section__subtitle">Consulta la disponibilidad actual de productos.</p>
            </header>

            <div class="admin-card">
                <c:choose>
                    <c:when test="${not empty requestScope.listaProductos}">
                        <div class="product-grid">
                            <c:forEach var="p" items="${requestScope.listaProductos}">
                                <div class="product-card ${p.stock < 5 ? 'product-card--low' : ''}">
                                    <div class="product-card__icon">&#128230;</div>
                                    <div class="product-card__info">
                                        <span class="product-card__name">${p.nombreProducto}</span>
                                        <span class="product-card__price">$<fmt:formatNumber value="${p.precioUnitario}" pattern="###,##0.00"/></span>
                                    </div>
                                    <div class="product-card__stock ${p.stock < 5 ? 'product-card__stock--low' : ''}">
                                        ${p.stock} uds
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <span class="empty-state__icon">&#128230;</span>
                            <p>No hay productos en inventario.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
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