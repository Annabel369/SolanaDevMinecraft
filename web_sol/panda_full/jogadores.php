<?php
session_start();
if (!isset($_SESSION["user"])) {
    header("Location: login.php");
    exit();
}

// Configurações do banco de dados do jogo (solanadev)
$host = "localhost";
$user = "root";
$password = "0073007";
$dbname_jogo = "solanadev";
$dbname_panda = "panda_full_db";

$conn_jogo = new mysqli($host, $user, $password, $dbname_jogo);
$conn_panda = new mysqli($host, $user, $password, $dbname_panda);

if ($conn_jogo->connect_error || $conn_panda->connect_error) {
    die("Erro ao conectar ao banco de dados.");
}

// Consulta jogadores do jogo e verifica no panda_full_db
$query = "SELECT j.nome, c.endereco, b.saldo FROM jogadores j 
          LEFT JOIN carteiras c ON j.id = c.jogador_id 
          LEFT JOIN banco b ON j.nome = b.jogador";
$result_jogo = $conn_jogo->query($query);
?>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Jogadores e Carteiras</title>
    <style>
        body { font-family: 'Inter', Arial, sans-serif; background-color: #f4f4f9; color: #333; margin: 0; padding: 20px; }
        h1 { text-align: center; color: #4CAF50; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: white; box-shadow: 0 4px 8px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #4CAF50; color: white; }
        tr:hover { background-color: #f1f1f1; }
        .status-ok { color: green; font-weight: bold; }
        .status-pending { color: orange; font-weight: bold; }
        .status-none { color: red; font-weight: bold; }
    </style>
</head>
<body>
    <h1>Verificação de Jogadores do Minecraft</h1>
    <table>
        <thead>
            <tr>
                <th>Jogador</th>
                <th>Carteira Solana</th>
                <th>Moedas no Jogo (Banco)</th>
                <th>Status Panda (Airdrop)</th>
            </tr>
        </thead>
        <tbody>
            <?php
            if ($result_jogo && $result_jogo->num_rows > 0) {
                while ($row = $result_jogo->fetch_assoc()) {
                    $nome = htmlspecialchars($row['nome']);
                    $endereco = htmlspecialchars($row['endereco'] ?? 'Sem carteira');
                    $saldo = htmlspecialchars($row['saldo'] ?? '0');
                    
                    $status_panda = "<span class='status-none'>Sem carteira</span>";
                    
                    if ($endereco !== 'Sem carteira') {
                        $query_panda = "SELECT last_transfer FROM transfer_log WHERE wallet_address = '" . $conn_panda->real_escape_string($endereco) . "'";
                        $result_panda = $conn_panda->query($query_panda);
                        
                        if ($result_panda && $result_panda->num_rows > 0) {
                            $panda_row = $result_panda->fetch_assoc();
                            $status_panda = "<span class='status-ok'>Recebeu em " . htmlspecialchars($panda_row['last_transfer']) . "</span>";
                        } else {
                            $status_panda = "<span class='status-pending'>Ainda não recebeu</span>";
                        }
                    }

                    echo "<tr>
                            <td>{$nome}</td>
                            <td>{$endereco}</td>
                            <td>{$saldo}</td>
                            <td>{$status_panda}</td>
                          </tr>";
                }
            } else {
                echo "<tr><td colspan='4'>Nenhum jogador encontrado no banco do jogo.</td></tr>";
            }
            ?>
        </tbody>
    </table>
</body>
</html>
<?php
$conn_jogo->close();
$conn_panda->close();
?>
