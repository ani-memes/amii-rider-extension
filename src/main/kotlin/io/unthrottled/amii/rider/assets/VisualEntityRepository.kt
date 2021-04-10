package io.unthrottled.amii.rider.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.actions.SyncedAssetsListener
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.platform.UpdateAssetsListener
import java.util.concurrent.ConcurrentHashMap

class VisualEntityRepository : Disposable {
  companion object {
    val instance: VisualEntityRepository
      get() = ApplicationManager.getApplication().getService(VisualEntityRepository::class.java)

    private const val ANIME_SYNC = 1
    private const val CHARACTER_SYNC = 2
    private const val VISUAL_SYNC = 4
    private const val ALL_SYNCED = ANIME_SYNC or CHARACTER_SYNC or VISUAL_SYNC
  }

  private var syncedAssets = 0

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
  init {
    LifeCycleManager.registerAssetUpdateListener(
      object : UpdateAssetsListener {
        override fun onRequestedUpdate() {
          syncedAssets = 0
        }

        override fun onRequestedBackgroundUpdate() {
          syncedAssets = 0
        }
      }
    )

    // there is a bit of a circular dependency between
    // the <code>VisualContentManager</code> and this
    // class, so we'll just register the update listener
    // after all of the services initialize.
    ApplicationManager.getApplication().invokeLater {
      messageBusConnection
        .subscribe(
          ContentManagerListener.TOPIC,
          ContentManagerListener {
            syncedAssets = syncedAssets or when (it) {
              AssetCategory.ANIME -> ANIME_SYNC
              AssetCategory.CHARACTERS -> CHARACTER_SYNC
              AssetCategory.VISUALS -> VISUAL_SYNC
              else -> syncedAssets
            }
            if (syncedAssets == ALL_SYNCED) {
              updateIndices()
              syncedAssets = 0
              ApplicationManager.getApplication().messageBus.syncPublisher(SyncedAssetsListener.TOPIC)
                .onSynced()
            }
          }
        )
    }
  }

  var visualAssetEntities: Map<String, VisualAssetEntity>
  private var allAnime: Map<String, AnimeEntity>
  private var characters: Map<String, CharacterEntity>

  init {
    allAnime = createAnimeIndex()
    characters = createCharacterIndex()
    visualAssetEntities = createIndex()
  }

  private fun updateIndices() {
    allAnime = createAnimeIndex()
    characters = createCharacterIndex()
    visualAssetEntities = createIndex()
  }

  val allCharacters: List<CharacterEntity>
    get() = characters.entries.map { it.value }

  private fun createAnimeIndex() = AnimeContentManager.supplyAssets().map { it.id to it.toEntity() }.toMap()
  private fun createCharacterIndex() = CharacterContentManager.supplyAssets()
    .filter { allAnime.containsKey(it.animeId) }
    .map { it.id to it.toEntity(allAnime[it.animeId]!!) }.toMap()

  private fun createIndex(): ConcurrentHashMap<String, VisualAssetEntity> {
    return ConcurrentHashMap(
      VisualContentManager.supplyAllAssetDefinitions()
        .map { visualRepresentation ->
          visualRepresentation.toEntity(visualRepresentation.char.mapNotNull { characters[it] })
        }.map { it.id to it }
        .toMap()
    )
  }

  override fun dispose() {
    messageBusConnection.dispose()
  }
}
