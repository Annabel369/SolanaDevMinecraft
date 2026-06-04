<?php
session_start();
if (!isset($_SESSION["user"])) {
    header("Location: login.php");
    exit();
}
?>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Painel de Controle</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #4CAF50;
            --primary-dark: #388E3C;
            --bg-color: #f4f7f6;
            --text-color: #333;
            --sidebar-bg: #2C3E50;
            --sidebar-text: #ECF0F1;
            --sidebar-hover: #34495E;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', sans-serif;
        }

        body {
            display: flex;
            height: 100vh;
            background-color: var(--bg-color);
            color: var(--text-color);
            overflow: hidden;
        }

        /* Sidebar Sidebar */
        .sidebar {
            width: 260px;
            background-color: var(--sidebar-bg);
            color: var(--sidebar-text);
            display: flex;
            flex-direction: column;
            transition: 0.3s;
        }

        .sidebar-header {
            padding: 20px;
            text-align: center;
            background-color: rgba(0,0,0,0.1);
            border-bottom: 1px solid rgba(255,255,255,0.05);
        }

        .sidebar-header h2 {
            font-size: 1.2rem;
            margin-bottom: 5px;
            color: var(--primary);
        }

        .sidebar-header p {
            font-size: 0.85rem;
            opacity: 0.8;
        }

        .menu {
            flex-grow: 1;
            padding: 15px 0;
            overflow-y: auto;
        }

        .menu-title {
            padding: 10px 20px;
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            opacity: 0.5;
            margin-top: 10px;
        }

        .menu a {
            display: flex;
            align-items: center;
            padding: 12px 20px;
            color: var(--sidebar-text);
            text-decoration: none;
            transition: 0.2s;
            border-left: 4px solid transparent;
        }

        .menu a:hover, .menu a.active {
            background-color: var(--sidebar-hover);
            border-left-color: var(--primary);
        }

        .logout-btn {
            background-color: #E74C3C;
            color: white;
            text-align: center;
            padding: 15px;
            text-decoration: none;
            font-weight: 600;
            transition: 0.2s;
        }

        .logout-btn:hover {
            background-color: #C0392B;
        }

        /* Content Area */
        .main-content {
            flex-grow: 1;
            display: flex;
            flex-direction: column;
        }

        .top-navbar {
            background: white;
            height: 60px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
            z-index: 10;
        }

        .iframe-container {
            flex-grow: 1;
            position: relative;
        }

        iframe {
            width: 100%;
            height: 100%;
            border: none;
            background: #fff;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .sidebar {
                width: 70px;
            }
            .sidebar-header h2, .sidebar-header p, .menu-title, .menu a span {
                display: none;
            }
        }
    </style>
</head>
<body>

    <!-- Sidebar -->
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>Panda Full Admin</h2>
            <p>Olá, <?php echo htmlspecialchars($_SESSION["user"]); ?>!</p>
        </div>
        
        <div class="menu">
            <div class="menu-title">Integração Jogo</div>
            <a href="jogadores.php" target="contentFrame" class="menu-item active" onclick="setActive(this)"><span>Jogadores (Status)</span></a>
            <a href="../caixa/carteira.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Sua Carteira</span></a>
            <a href="../caixa/transferi_p.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Transferências</span></a>
            
            <div class="menu-title">Solana Token</div>
            <a href="panda.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Airdrop / Saldo Panda</span></a>
            <a href="buy.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Pagamentos</span></a>
            
            <div class="menu-title">Administração</div>
            <a href="../caixa/index.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Livro Caixa</span></a>
            <a href="admin/heysolana.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Painel Docker (CLI)</span></a>
            <a href="registro.php" target="contentFrame" class="menu-item" onclick="setActive(this)"><span>Gerenciar Usuários</span></a>
        </div>
        
        <div style="padding: 10px; text-align: center;">
            <script type='text/javascript' src='https://storage.ko-fi.com/cdn/widget/Widget_2.js'></script>
            <script type='text/javascript'>kofiwidget2.init('Apoiar no Ko-fi', '#4CAF50', 'H2H411P12P');kofiwidget2.draw();</script> 
        </div>

        <a href="logout.php" class="logout-btn">Sair do Sistema</a>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
        <div class="top-navbar">
            <h3 id="page-title">Jogadores (Status)</h3>
        </div>
        
        <div class="iframe-container">
            <!-- Abre por padrao a nova pagina de jogadores -->
            <iframe name="contentFrame" id="contentFrame" src="jogadores.php"></iframe>
        </div>
    </main>

    <script>
        function setActive(element) {
            // Remove active class from all
            document.querySelectorAll('.menu-item').forEach(el => el.classList.remove('active'));
            // Add to clicked
            element.classList.add('active');
            // Update Top Bar Title
            document.getElementById('page-title').innerText = element.innerText;
        }
    </script>
</body>
</html>
