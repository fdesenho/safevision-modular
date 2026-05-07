from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.propagate import set_global_textmap
from opentelemetry.propagators.b3 import B3MultiFormat
import config

def setup_telemetry():
    # 1. Força a propagação de contexto usando B3 Headers (Compatibilidade com Micrometer/Zipkin)
    set_global_textmap(B3MultiFormat())

    # 2. Configura o exportador apontando para a URL dinâmica do .env
    zipkin_exporter = ZipkinExporter(
        endpoint=f"{config.ZIPKIN_URL}/api/v2/spans",
    )
    
    provider = TracerProvider()
    provider.add_span_processor(BatchSpanProcessor(zipkin_exporter))
    trace.set_tracer_provider(provider)
    
    return trace.get_tracer("safevision.vision-agent")

tracer = setup_telemetry()