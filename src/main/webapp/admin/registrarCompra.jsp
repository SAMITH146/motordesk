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
    <title>Registrar Ingreso de Producto | MotorDesk</title>
</head>

<body>
    <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk"
                class="navbar__logo-img" />
        </div>

        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item active">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>
        </nav>

        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
                <span class="navbar__user-role">${sessionScope.usuarioLogueado.idCargo}</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png"
                    alt="Cerrar sesión" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">
        <section class="admin-section" style="max-width: 800px; margin: 0 auto;">
            <header style="margin-bottom: 2rem;">
                <h1 class="admin-section__title">Ingreso Creador de Producto</h1>
                <p class="admin-section__subtitle">Añade stock a un producto existente o crea uno nuevo en el acto.</p>
            </header>

            <c:if test="${not empty requestScope.mensaje}">
                <div class="alert alert-success" style="background:#2ecc71; color:white; padding:15px; border-radius:5px; margin-bottom:20px;">
                    ${requestScope.mensaje}
                </div>
            </c:if>
            <c:if test="${not empty requestScope.mensajeError}">
                <div class="alert alert-error" style="background:#e74c3c; color:white; padding:15px; border-radius:5px; margin-bottom:20px;">
                    ${requestScope.mensajeError}
                </div>
            </c:if>

            <article class="admin-card">
                <form action="${pageContext.request.contextPath}/admin/ingreso" method="post" style="display: flex; flex-direction: column; gap: 1.5rem;">
                    
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <label for="nombreProveedor" style="font-weight: 600;">Nombre del Proveedor (Seleccione o escriba uno nuevo)</label>
                        <input list="proveedoresList" name="nombreProveedor" id="nombreProveedor" required class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" placeholder="Escriba el nombre del proveedor...">
                        <datalist id="proveedoresList">
                            <c:forEach var="prov" items="${requestScope.proveedores}">
                                <option value="${prov.nombreProveedor}"></option>
                            </c:forEach>
                        </datalist>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <label for="nombreProducto" style="font-weight: 600;">Nombre del Producto</label>
                        <input list="productosList" name="nombreProducto" id="nombreProducto" required class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" placeholder="Escriba el nombre exacto del producto o uno nuevo...">
                        <datalist id="productosList">
                            <c:forEach var="prod" items="${requestScope.productos}">
                                <option value="${prod.nombreProducto}"></option>
                            </c:forEach>
                        </datalist>
                        <small style="color: rgba(255,255,255,0.6); margin-top: -5px;">Si escribes un producto nuevo, se creará usando las categorías de abajo. Si ya existe, se sumará el stock al existente.</small>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
                        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                            <label for="tipoVehiculo" style="font-weight: 600;">Vehiculo</label>
                            <select name="tipoVehiculo" id="tipoVehiculo" class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" required>
                                <option value="Moto">Moto</option>
                                <option value="Carro">Carro</option>
                                <option value="Tractomula">Tractomula</option>
                            </select>
                        </div>
                        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                            <label for="seccion" style="font-weight: 600;">Seccion</label>
                            <select name="seccion" id="seccion" class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" required>
                                <option value="Llantas">Llantas</option>
                                <option value="Frenos">Frenos</option>
                                <option value="Motor">Motor</option>
                                <option value="Arrastre">Arrastre</option>
                                <option value="Suspension">Suspension</option>
                                <option value="Lubricantes">Lubricantes</option>
                                <option value="Electrico">Partes Eléctricas</option>
                                <option value="Otros">Otros / Accesorios</option>
                            </select>
                        </div>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <label for="precioVenta" style="font-weight: 600;">Precio de Venta al Publico (Actualizará o Creará el producto)</label>
                        <input type="text" name="precioVenta" id="precioVenta" required class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" placeholder="Ej: 50.00">
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
                        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                            <label for="cantidad" style="font-weight: 600;">Cantidad que ingresa</label>
                            <input type="number" name="cantidad" id="cantidad" min="1" required class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" placeholder="Ej: 50">
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                            <label for="costoUnitario" style="font-weight: 600;">Costo de Compra (Por Unidad)</label>
                            <input type="text" name="costoUnitario" id="costoUnitario" required class="admin-btn" style="text-align: left; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1);" placeholder="Ej: 15.50">
                        </div>
                    </div>

                    <div style="margin-top: 1rem;">
                        <button type="submit" class="admin-btn" style="width: 100%; display: block; text-align: center; background: #3498db; border-color: #3498db;">Confirmar Ingreso y Actualizar Stock</button>
                    </div>

                </form>
            </article>
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
