<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.1" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css" />

    <title>Gestión de Clientes | MotorDesk</title>
    <style>
        .admin-alert {
            padding: 1rem;
            margin-bottom: 1.5rem;
            border-radius: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            animation: slideIn 0.3s ease-out;
        }
        .admin-alert--success { background-color: rgba(46, 204, 113, 0.2); border: 1px solid #2ecc71; color: #2ecc71; }
        .admin-alert--error { background-color: rgba(231, 76, 60, 0.2); border: 1px solid #e74c3c; color: #e74c3c; }
        .admin-alert--info { background-color: rgba(52, 152, 219, 0.2); border: 1px solid #3498db; color: #3498db; }
        .admin-alert__close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7; }
        .admin-alert__close:hover { opacity: 1; }
        @keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

        input[type=number]::-webkit-inner-spin-button,
        input[type=number]::-webkit-outer-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }
        input[type=number] {
            -moz-appearance: textfield;
        }
    </style>
</head>

<body>
        <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
        </div>
        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item active">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProveedorController" class="navbar__menu-item">Proveedores</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>

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

    <main class="admin-main fade-in">
        <!-- Alertas de feedback -->
        <c:if test="${not empty sessionScope.mensaje}">
            <div class="admin-alert admin-alert--${sessionScope.tipoMensaje}">
                <span class="admin-alert__text">${sessionScope.mensaje}</span>
                <button class="admin-alert__close" onclick="this.parentElement.remove();">×</button>
            </div>
            <% session.removeAttribute("mensaje"); session.removeAttribute("tipoMensaje"); %>
        </c:if>

                            <!-- Aquí renderizamos el input para ingresar o mostrar el documento del cliente -->
                            <input class="admin-form__input" type="text" id="doc_cliente" name="doc_cliente" required
                                placeholder="Ej: 123456789" value="${requestScope.clienteEditar.documento}" readonly
                                minlength="6" maxlength="15" pattern="\d+" title="Solo números" oninput="this.value = this.value.replace(/[^0-9]/g, '')">
                        </div>

                        <div class="admin-form__group">
                            <label class="admin-form__label" for="nom_cliente">Nombre Completo:</label>
                            <!-- Aquí renderizamos el input para ingresar o editar el nombre del cliente -->
                            <input class="admin-form__input" type="text" id="nom_cliente" name="nom_cliente"
                                required placeholder="Nombre y Apellidos" value="${requestScope.clienteEditar.nombre}" autocomplete="off"
                                pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" title="Solo se permiten letras y espacios" oninput="this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '')">
                        </div>

                        <div class="admin-form__group">
                            <label class="admin-form__label" for="direccion_cliente">Dirección:</label>
                            <!-- Aquí renderizamos el input para registrar o actualizar la dirección del cliente -->
                            <input class="admin-form__input" type="text" id="direccion_cliente" name="direccion_cliente"
                                required placeholder="Dirección del cliente" value="${requestScope.clienteEditar.direccion}" autocomplete="off">
                        </div>

                        <div class="admin-form__actions">
                            <a href="${pageContext.request.contextPath}/ClienteController" class="admin-btn admin-btn--danger" style="text-decoration: none; display: flex; align-items: center; justify-content: center;">Cancelar</a>
                            <button type="submit" class="admin-btn">Actualizar Cambios</button>
                        </div>

                    </form>
                </article>
            </c:if>
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

