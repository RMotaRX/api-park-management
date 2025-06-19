CREATE INDEX IF NOT EXISTS idx_veiculos_placa ON veiculos (placa_veiculo);

CREATE INDEX IF NOT EXISTS idx_setor_aberto ON setor (aberto);
CREATE INDEX IF NOT EXISTS idx_setor_ocupado ON setor (ocupacao_atual);
CREATE INDEX IF NOT EXISTS idx_setor_capacidade ON setor (capacidade_maxima);

CREATE INDEX IF NOT EXISTS idx_vaga_coordinates ON vaga (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_vaga_setor ON vaga (id_setor);
CREATE INDEX IF NOT EXISTS idx_vaga_ocupada ON vaga (ocupada);

CREATE INDEX IF NOT EXISTS idx_sessoes_placa_veiculo ON sessoes_de_estacionamento (placa_veiculo);
CREATE INDEX IF NOT EXISTS idx_sessoes_status ON sessoes_de_estacionamento (status);
CREATE INDEX IF NOT EXISTS idx_sessoes_hora_entrada ON sessoes_de_estacionamento (hora_entrada);
CREATE INDEX IF NOT EXISTS idx_sessoes_hora_saida ON sessoes_de_estacionamento (hora_saida);
CREATE INDEX IF NOT EXISTS idx_sessoes_vaga ON sessoes_de_estacionamento (id_vaga);
CREATE INDEX IF NOT EXISTS idx_sessoes_tempo_estacionado ON sessoes_de_estacionamento (tempo_estacionado);

CREATE OR REPLACE VIEW sessoes_ativas_view AS
SELECT
  sde.id as sessao_id,
  sde.placa_veiculo,
  sde.hora_entrada,
  sde.tempo_estacionado,
  sde.preco_entrada,
  sde.status,
  v.latitude,
  v.longitude,
  s.id as setor_id,
  s.preco_base as preco_base_setor
FROM sessoes_de_estacionamento sde
LEFT JOIN vaga v ON sde.id_vaga = v.id
LEFT JOIN setor s ON v.id_setor = s.id
WHERE sde.status IN ('ENTRADA', 'ESTACIONADO');

CREATE OR REPLACE VIEW receita_view AS
SELECT
  DATE(sde.hora_saida) as data_receita,
  s.id as setor_id,
  SUM(sde.preco_final) as receita_total,
  COUNT(*) as total_sessoes
FROM sessoes_de_estacionamento sde
JOIN vaga v ON sde.id_vaga = v.id
JOIN setor s ON v.id_setor = s.id
WHERE sde.status = 'SAIDA' AND sde.preco_final IS NOT NULL
GROUP BY DATE(sde.hora_saida), s.id;

CREATE OR REPLACE VIEW ocupacao_setor_view AS
SELECT
  s.id as setor_id,
  s.ocupacao_atual,
  s.capacidade_maxima,
  ROUND((s.ocupacao_atual::decimal / s.capacidade_maxima::decimal) * 100, 2) as percentual_ocupacao,
  (s.capacidade_maxima - s.ocupacao_atual) as vagas_disponiveis,
  s.aberto,
  s.preco_base
FROM setor s;

CREATE OR REPLACE VIEW vagas_disponiveis_view AS
SELECT
  v.id as vaga_id,
  v.latitude,
  v.longitude,
  v.ocupada,
  s.id as setor_id,
  s.preco_base,
  s.aberto as setor_aberto,
  s.ocupacao_atual,
  s.capacidade_maxima
FROM vaga v
JOIN setor s ON v.id_setor = s.id
WHERE v.ocupada = false AND s.aberto = true;

CREATE OR REPLACE VIEW analise_duracao_view AS
SELECT
  sde.id as sessao_id,
  sde.placa_veiculo,
  sde.status,
  EXTRACT(EPOCH FROM (sde.tempo_estacionado - sde.hora_entrada))/60 as minutos_ate_estacionar,
  CASE
    WHEN sde.hora_saida IS NOT NULL AND sde.tempo_estacionado IS NOT NULL THEN
      EXTRACT(EPOCH FROM (sde.hora_saida - sde.tempo_estacionado))/60
    ELSE NULL
  END as minutos_estacionado,
  CASE
    WHEN sde.hora_saida IS NOT NULL THEN
      EXTRACT(EPOCH FROM (sde.hora_saida - sde.hora_entrada))/60
    ELSE NULL
  END as minutos_total,
  s.id as setor_id
FROM sessoes_de_estacionamento sde
LEFT JOIN vaga v ON sde.id_vaga = v.id
LEFT JOIN setor s ON v.id_setor = s.id