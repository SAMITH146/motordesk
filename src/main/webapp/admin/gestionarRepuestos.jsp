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
    <style>
        .admin-alert { padding: 1rem; margin-bottom: 1.5rem; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; animation: slideIn 0.3s ease-out; }
        .admin-alert--success { background-color: rgba(46, 204, 113, 0.2); border: 1px solid #2ecc71; color: #2ecc71; }
        .admin-alert--error { background-color: rgba(231, 76, 60, 0.2); border: 1px solid #e74c3c; color: #e74c3c; }
        .admin-alert__close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7; }
        @keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    </style>
    <title>Gestion de Productos | MotorDesk</title>
</head>
<body>
    <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
        </div>
        <nav class="navbar__menu">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecanicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item active">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Ordenes</a>
        </nav>
        <div class="navbar__session">
            <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesion" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">
        <header class="admin-section__header">
            <h1 class="admin-section__title">Inventario de Productos</h1>
            <p class="admin-section__subtitle">Gestion centralizada de productos y herramientas del taller.</p>
        </header>

        <!-- Feedback Alert -->
        <c:if test="${not empty sessionScope.mensaje}">
            <div class="admin-alert admin-alert--${sessionScope.tipoMensaje}">
                <span class="admin-alert__text">${sessionScope.mensaje}</span>
                <button class="admin-alert__close" onclick="this.parentElement.remove();">x</button>
            </div>
            <% session.removeAttribute("mensaje"); session.removeAttribute("tipoMensaje"); %>
        </c:if>

        <section class="admin-section">
            <c:if test="${not empty requestScope.productoEditar}">
            <div class="admin-form-section">
                <h3 class="admin-form-section__title">Editar Producto</h3>
                <form action="${pageContext.request.contextPath}/ProductoController" method="post" class="admin-form">
                    <input type="hidden" name="id" value="${requestScope.productoEditar.idProducto}" />
                    
                    <div class="admin-form__group">
                        <label class="admin-form__label">Nombre del Producto</label>
                        <input type="text" name="nombre" class="admin-form__input" placeholder="Ej: Neumatico" 
                               value="${requestScope.productoEditar.nombreProducto}" required />
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                        <div class="admin-form__group" style="margin-bottom: 0;">
                            <label class="admin-form__label">Vehiculo</label>
                            <select name="tipoVehiculo" class="admin-form__input" required>
                                <option value="Moto" ${requestScope.productoEditar.tipoVehiculo == 'Moto' ? 'selected' : ''}>Moto</option>
                                <option value="Carro" ${requestScope.productoEditar.tipoVehiculo == 'Carro' ? 'selected' : ''}>Carro</option>
                                <option value="Tractomula" ${requestScope.productoEditar.tipoVehiculo == 'Tractomula' ? 'selected' : ''}>Tractomula</option>
                            </select>
                        </div>
                        <div class="admin-form__group" style="margin-bottom: 0;">
                            <label class="admin-form__label">Seccion</label>
                            <select name="seccion" class="admin-form__input" required>
                                <option value="Llantas" ${requestScope.productoEditar.seccion == 'Llantas' ? 'selected' : ''}>Llantas</option>
                                <option value="Frenos" ${requestScope.productoEditar.seccion == 'Frenos' ? 'selected' : ''}>Frenos</option>
                                <option value="Motor" ${requestScope.productoEditar.seccion == 'Motor' ? 'selected' : ''}>Motor</option>
                                <option value="Arrastre" ${requestScope.productoEditar.seccion == 'Arrastre' ? 'selected' : ''}>Arrastre</option>
                                <option value="Suspension" ${requestScope.productoEditar.seccion == 'Suspension' ? 'selected' : ''}>Suspension</option>
                                <option value="Lubricantes" ${requestScope.productoEditar.seccion == 'Lubricantes' ? 'selected' : ''}>Lubricantes</option>
                                <option value="Otros" ${requestScope.productoEditar.seccion == 'Otros' ? 'selected' : ''}>Otros / Accesorios</option>
                            </select>
                        </div>
                    </div>

                    <div class="admin-form__group">
                        <label class="admin-form__label">Stock Actual</label>
                        <input type="number" name="stock" class="admin-form__input" value="${not empty requestScope.productoEditar ? requestScope.productoEditar.stock : '0'}" required />
                    </div>
                    
                    <div class="admin-form__group">
                        <label class="admin-form__label">Precio Unitario ($)</label>
                        <input type="number" step="0.01" name="precio" class="admin-form__input" value="${not empty requestScope.productoEditar ? requestScope.productoEditar.precioUnitario : '0.00'}" required />
                    </div>

                    <div class="admin-form__actions">
                        <button type="submit" class="admin-btn">${empty requestScope.productoEditar ? 'Guardar Producto' : 'Actualizar Cambios'}</button>
                        <c:if test="${not empty requestScope.productoEditar}">
                            <a href="${pageContext.request.contextPath}/ProductoController" class="admin-btn admin-btn--danger">Cancelar</a>
                        </c:if>
                    </div>
                </form>
            </div>
            </c:if>

            <div class="admin-table-container" style="margin-top: 2rem;">
                <div style="padding: 20px 20px 0 20px;">
                    <h3 class="admin-form-section__title" style="border-bottom: none; display: inline-block;">Listado de Productos</h3>
                    
                    <!-- Búsqueda y Filtros -->
                    <form action="${pageContext.request.contextPath}/ProductoController" method="get" style="display: flex; gap: 10px; margin-top: 10px; flex-wrap: wrap;">
                        <input type="text" name="buscar" placeholder="Buscar por Nombre..." value="${requestScope.filtroBuscar}" class="admin-form__input" style="flex: 2; min-width: 200px;">
                        
                        <select name="f_vehiculo" class="admin-form__input" style="flex: 1; min-width: 150px;">
                            <option value="">Todos los Vehiculos</option>
                            <option value="Moto" ${requestScope.filtroVehiculo == 'Moto' ? 'selected' : ''}>Moto</option>
                            <option value="Carro" ${requestScope.filtroVehiculo == 'Carro' ? 'selected' : ''}>Carro</option>
                            <option value="Tractomula" ${requestScope.filtroVehiculo == 'Tractomula' ? 'selected' : ''}>Tractomula</option>
                        </select>

                        <select name="f_seccion" class="admin-form__input" style="flex: 1; min-width: 150px;">
                            <option value="">Todas las Secciones</option>
                            <option value="Llantas" ${requestScope.filtroSeccion == 'Llantas' ? 'selected' : ''}>Llantas</option>
                            <option value="Frenos" ${requestScope.filtroSeccion == 'Frenos' ? 'selected' : ''}>Frenos</option>
                            <option value="Motor" ${requestScope.filtroSeccion == 'Motor' ? 'selected' : ''}>Motor</option>
                            <option value="Arrastre" ${requestScope.filtroSeccion == 'Arrastre' ? 'selected' : ''}>Arrastre</option>
                            <option value="Suspension" ${requestScope.filtroSeccion == 'Suspension' ? 'selected' : ''}>Suspension</option>
                            <option value="Lubricantes" ${requestScope.filtroSeccion == 'Lubricantes' ? 'selected' : ''}>Lubricantes</option>
                            <option value="Electrico" ${requestScope.filtroSeccion == 'Electrico' ? 'selected' : ''}>Partes Eléctricas</option>
                            <option value="Otros" ${requestScope.filtroSeccion == 'Otros' ? 'selected' : ''}>Otros</option>
                        </select>
                        
                        <button type="submit" class="admin-btn" style="flex: 0 1 auto; white-space: nowrap;">🔍 Buscar</button>
                        <a href="${pageContext.request.contextPath}/ProductoController" class="admin-btn admin-btn--danger" style="flex: 0 1 auto;">Limpiar</a>
                    </form>
                </div>

                <table class="admin-table" style="margin-top: 15px;">
                    <thead class="admin-table__head">
                        <tr>
                            <th class="admin-table__th">ID</th>
                            <th class="admin-table__th">Nombre</th>
                            <th class="admin-table__th">Categorizacion</th>
                            <th class="admin-table__th">Stock</th>
                            <th class="admin-table__th">Precio Venta</th>
                            <th class="admin-table__th">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty requestScope.listaProductos}">
                                <c:forEach var="p" items="${requestScope.listaProductos}">
                                    <tr class="admin-table__row">
                                        <td class="admin-table__td">#${p.idProducto}</td>
                                        <td class="admin-table__td"><strong>${p.nombreProducto}</strong></td>
                                        <td class="admin-table__td">
                                            <span style="display: block; font-size: 0.85em; opacity: 0.8;">[${p.tipoVehiculo != null ? p.tipoVehiculo : 'General'}]</span>
                                            <span style="font-weight: bold; color: #3498db;">${p.seccion != null ? p.seccion : 'General'}</span>
                                        </td>
                                        <td class="admin-table__td">
                                            <span class="admin-badge ${p.stock <= 5 ? 'admin-badge--inactive' : 'admin-badge--active'}">
                                                ${p.stock} uds.
                                            </span>
                                        </td>
                                        <td class="admin-table__td"><fmt:formatNumber value="${p.precioUnitario}" type="currency" currencySymbol="$" /></td>
                                        <td class="admin-table__td">
                                            <div class="admin-table__actions">
                                                <a href="${pageContext.request.contextPath}/ProductoController?action=edit&id=${p.idProducto}" class="admin-action-btn admin-action-btn--edit">Editar</a>
                                                <a href="#deleteModalProd-${p.idProducto}" class="admin-action-btn admin-action-btn--delete">Borrar</a>

                                                <!-- Delete Modal (CSS Only) -->
                                                <div id="deleteModalProd-${p.idProducto}" class="modal-css">
                                                    <div class="modal-content-css">
                                                        <h2>¿Eliminar Producto?</h2>
                                                        <p>Estás a punto de eliminar <strong>${p.nombreProducto}</strong>.<br>Esta acción no se puede deshacer.</p>
                                                        <div class="modal-buttons-css">
                                                            <a href="#" class="btn-modal-css btn-modal-css--cancel">Cancelar</a>
                                                            <form action="${pageContext.request.contextPath}/ProductoController" method="post" style="display:inline;">
                                                                <input type="hidden" name="action" value="delete" />
                                                                <input type="hidden" name="id" value="${p.idProducto}" />
                                                                <button type="submit" class="btn-modal-css btn-modal-css--confirm">Sí, Eliminar</button>
                                                            </form>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr class="admin-table__row">
                                    <td class="admin-table__td" colspan="6" style="text-align: center;">No hay productos registrados.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
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
</body>
</html>
