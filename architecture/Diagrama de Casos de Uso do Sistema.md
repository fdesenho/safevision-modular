flowchart TD 
Op["Usuário Protegido"] 
subgraph SafeVision["Ecossistema SafeVision"] 
UC1["Autenticar no Sistema"] 
UC2["Ativar/Desativar Monitoramento"] 
UC3["Receber Alerta de Objeto/Comportamento"] 
UC4["Visualizar Evidência (Snapshot)"] 
UC5["Consultar Histórico"] 
end 
Op --> UC1 
Op --> UC2 
Op --> UC3 
Op --> UC5 
UC3 -.->|extend| UC4